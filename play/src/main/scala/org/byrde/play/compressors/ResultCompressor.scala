package org.byrde.play.compressors

import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.util.ByteString

import com.googlecode.htmlcompressor.compressor.Compressor

import play.api.http.HeaderNames._
import play.api.http.{HttpEntity, HttpProtocol}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ResultCompressor[C <: Compressor](implicit mat: Materializer) {
  /**
    * The compressor instance.
    */
  val compressor: C

  /**
    * Check if the given result is a compressible result.
    *
    * @param result The result to check.
    * @return True if the result is a compressible result, false otherwise.
    */
  def isCompressible(result: Result): Boolean = {
    val isChunked = result.header.headers
      .get(TRANSFER_ENCODING)
      .contains(HttpProtocol.CHUNKED)
    val isGzipped = result.header.headers.get(CONTENT_ENCODING).contains("gzip")
    val ret       = !isChunked && !isGzipped

    ret
  }

  /**
    * Compress the result.
    *
    * @param result The result to compress.
    * @return The compressed result.
    */
  def compressResult(result: Result): Future[Result] = {
    def compress(data: ByteString) =
      compressor.compress(data.decodeString("UTF-8").trim).getBytes("UTF-8")

    if (isCompressible(result)) {
      result.body match {
        case HttpEntity.Strict(data, contentType) =>
          Future.successful(
            Result(result.header,
                   HttpEntity.Strict(ByteString(compress(data)), contentType)))
        case HttpEntity.Streamed(data, _, _) =>
          data.toMat(Sink.fold(ByteString())(_ ++ _))(Keep.right).run() map {
            bytes =>
              val compressed = compress(bytes)
              val length     = compressed.length
              Result(
                result.header.copy(headers = result.header.headers),
                HttpEntity.Streamed(Source.single(ByteString(compressed)),
                                    Some(length.toLong),
                                    result.body.contentType)
              )
          }
        case _ =>
          Future.successful(result)
      }
    } else {
      Future.successful(result)
    }
  }
}
