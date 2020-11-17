package org.byrde.tapir.support

import org.byrde.tapir._

import io.circe.{Decoder, Encoder}

import sttp.tapir.{Schema, Validator}

import scala.concurrent.{ExecutionContext, Future}

trait RouteSupport extends RequestSupport with ResponseSupport with RequestIdSupport {
  def SuccessCode: Int
  
  def ErrorCode: Int
  
  implicit class RichResponse[T, TT](future: Future[Either[TT, T]]) {
    def toOut[A <: TapirResponse](
      success: (T, Int) => A,
      error: (TT, Int) => TapirErrorResponse =
        (_, code) => TapirResponse.Default(code)
    )(
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
      ec: ExecutionContext
    ): Future[Either[TapirErrorResponse, A]] =
      future.map {
        case Right(succ) =>
          Right(success(succ, SuccessCode))

        case Left(err) =>
          Left(error(err, ErrorCode))
      }
  }
}
