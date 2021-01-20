package org.byrde.http.server.support

import akka.http.scaladsl.model._

import org.byrde.http.server.{Body, Header, JsonErrorResponse}

import io.circe.Encoder
import io.circe.generic.auto._

import sttp.client3

trait ResponseSupport {
  implicit class SttpResponse2HttpResponse(response: client3.Response[Either[String, String]]) {
    def toHttpResponse[O](
      responseHeaders: Seq[Header] => Seq[Header],
      handleSuccess: String => Either[JsonErrorResponse, O],
      handleFailure: String => Either[JsonErrorResponse, O],
    )(implicit encoder: Encoder[O]): HttpResponse =
      HttpResponse(
        response.code.code,
        responseHeaders(response.headers.map(Header.fromSttpHeader)).map(_.toHttpHeader),
        Body
          .fromSttpResponseBody(response.body, handleSuccess, handleFailure)
          .fold(_.toHttpEntity, _.toHttpEntity)
      )
  }
}


