package org.byrde.http.server

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.util.ByteString

import io.circe.syntax._
import io.circe.{Encoder, Printer}

case class Body[O](value: O) {
  def toHttpEntity(implicit encoder: Encoder[O]): HttpEntity.Strict =
    HttpEntity(
      `application/json`,
      ByteString {
        Printer.noSpaces.printToByteBuffer(
          value.asJson,
          `application/json`.charset.nioCharset()
        )
      }
    )
}

object Body {
  def fromSttpResponseBody[O](
    body: Either[String, String],
    handleSuccess: String => Either[ErrorResponse, O],
    handleFailure: String => Either[ErrorResponse, O],
  ): Either[Body[ErrorResponse], Body[O]] =
    body match {
      case Right(value) =>
        handleSuccess(value).map(Body.apply).left.map(Body.apply)

      case Left(value) =>
        handleFailure(value).map(Body.apply).left.map(Body.apply)
    }
}