package org.byrde.play.compressors.impl

import java.util.zip.GZIPOutputStream

import akka.stream.Materializer
import akka.util.ByteString

import play.api.http.HeaderNames.{ACCEPT_ENCODING, CONTENT_ENCODING, VARY}
import play.api.http.{HttpChunk, HttpEntity, Status}
import play.api.libs.streams.GzipFlow
import play.api.mvc.{Headers, RequestHeader, ResponseHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

case class GzipCompressor()(implicit ec: ExecutionContext, mat: Materializer) {

  def isCompressible(request: RequestHeader): Boolean =
    request.method != "HEAD" && gzipIsAcceptedAndPreferredBy(request)

  def handleResult(request: RequestHeader, result: Result): Future[Result] = {
    if (shouldCompress(result)) {
      val header =
        result.header.copy(headers = setupHeader(result.header.headers))

      result.body match {
        case HttpEntity.Strict(data, contentType) =>
          Future.successful(
            Result(header, compressStrictEntity(data, contentType)))

        case entity @ HttpEntity.Streamed(_, Some(_), contentType) =>
          // It's below the chunked threshold, so buffer then compress and send
          entity.consumeData.map { data =>
            Result(header, compressStrictEntity(data, contentType))
          }

        case HttpEntity.Streamed(data, _, contentType) =>
          // It's above the chunked threshold, compress through the gzip flow, and send as chunked
          val gzipped =
            data via GzipFlow.gzip(1024) map (d => HttpChunk.Chunk(d))

          Future.successful(
            Result(header, HttpEntity.Chunked(gzipped, contentType)))

        case HttpEntity.Chunked(chunks, contentType) =>
          Future.successful(result)
      }
    } else {
      Future.successful(result)
    }
  }

  private def compressStrictEntity(data: ByteString,
                                   contentType: Option[String]) = {
    val builder =
      ByteString.newBuilder

    val gzipOs =
      new GZIPOutputStream(builder.asOutputStream, 1024, true)

    gzipOs.write(data.toArray)
    gzipOs.close()
    HttpEntity.Strict(builder.result(), contentType)
  }

  /**
    * Whether this request may be compressed.
    */
  private def gzipIsAcceptedAndPreferredBy(request: RequestHeader) = {
    val codings = acceptHeader(request.headers, ACCEPT_ENCODING)

    def explicitQValue(coding: String) =
      codings collectFirst {
        case (q, c) if c equalsIgnoreCase coding => q
      }

    def defaultQValue(coding: String) =
      if (coding == "identity")
        0.001d
      else
        0d

    def qvalue(coding: String) =
      explicitQValue(coding) orElse explicitQValue("*") getOrElse defaultQValue(coding)

    qvalue("gzip") > 0d && qvalue("gzip") >= qvalue("identity")
  }

  /**
    * Whether this response should be compressed.  Responses that may not contain content won't be compressed, nor will
    * responses that already define a content encoding.  Empty responses also shouldn't be compressed, as they will
    * actually always get bigger.
    */
  private def shouldCompress(result: Result) =
    isAllowedContent(result.header) &&
      isNotAlreadyCompressed(result.header) &&
      !result.body.isKnownEmpty

  /**
    * Certain response codes are forbidden by the HTTP spec to contain content, but a gzipped response always contains
    * a minimum of 20 bytes, even for empty responses.
    */
  private def isAllowedContent(header: ResponseHeader) =
    header.status != Status.NO_CONTENT && header.status != Status.NOT_MODIFIED

  /**
    * Of course, we don't want to double compress responses
    */
  private def isNotAlreadyCompressed(header: ResponseHeader) =
    header.headers.get(CONTENT_ENCODING).isEmpty

  private def setupHeader(header: Map[String, String]): Map[String, String] =
    header + (CONTENT_ENCODING -> "gzip") + addToVaryHeader(header,
                                                            VARY,
                                                            ACCEPT_ENCODING)

  /**
    * There may be an existing Vary value, which we must add to (comma separated)
    */
  private def addToVaryHeader(existingHeaders: Map[String, String],
                              headerName: String,
                              headerValue: String): (String, String) = {
    existingHeaders.get(headerName) match {
      case None =>
        (headerName, headerValue)

      case Some(existing) if existing.split(",").exists(_.trim.equalsIgnoreCase(headerValue)) =>
        (headerName, existing)

      case Some(existing) =>
        (headerName, s"$existing,$headerValue")
    }
  }

  private def acceptHeader(headers: Headers,
                           headerName: String): Seq[(Double, String)] = {
    for {
      header <- headers.get(headerName).toList
      value0 <- header.split(',')
      value = value0.trim
    } yield {
      RequestHeader.qPattern.findFirstMatchIn(value) match {
        case Some(m) =>
          (m.group(1).toDouble, m.before.toString)

        case None =>
          (1.0, value) // “The default value is q=1.”
      }
    }
  }

}
