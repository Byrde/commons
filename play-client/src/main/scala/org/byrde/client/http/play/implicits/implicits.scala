package org.byrde.client.http.play

import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, StandaloneWSResponse}
import play.api.mvc

import org.byrde.client.http.HttpClientError.{HttpParsingError, HttpResponseError}
import org.byrde.client.http._
import org.byrde.client.http.play.support.{BodyWritableSupport, ProxyRequestSupport, ResponseSupport}
import org.byrde.service.response.ServiceResponse
import org.byrde.uri.{Path, Url}

import io.circe.{Decoder, Json}

import zio.{IO, ZIO}

package object implicits extends BodyWritableSupport with ResponseSupport with ProxyRequestSupport {

  implicit def proxy__RequestEncoder[T: BodyWritable]: RequestEncoder[PlayService, mvc.Request[T], StandaloneWSRequest] =
    new RequestEncoder[PlayService, mvc.Request[T], StandaloneWSRequest] {
      override def encode(request: mvc.Request[T])(implicit env: PlayService): StandaloneWSRequest =
        request.toWSRequest(env.url(Url(env.host, Path.fromString(request.path)).toString))
    }

  implicit def generic__RequestEncoder[T: BodyWritable]: RequestEncoder[PlayService, Request[T], StandaloneWSRequest] =
    new RequestEncoder[PlayService, Request[T], StandaloneWSRequest] {
      override def encode(request: Request[T])(implicit env: PlayService): StandaloneWSRequest =
        env
          .url(request.url.toString)
          .withBody(request.body)
          .withMethod(request.method)
          .withHttpHeaders(request.headers.toSeq: _*)
    }

  implicit val json__RequestEncoder: RequestEncoder[PlayService, Request[Json], StandaloneWSRequest] =
    new RequestEncoder[PlayService, Request[Json], StandaloneWSRequest] {
      override def encode(request: Request[Json])(implicit env: PlayService): StandaloneWSRequest =
        env
          .url(request.url.toString)
          .withBody(request.body)
          .withMethod(request.method)
          .withHttpHeaders(request.headers.toSeq: _*)
    }

  implicit def generic__ResponseDecoder[T: Decoder]: ResponseDecoder[PlayService, StandaloneWSResponse, T] =
    new ResponseDecoder[PlayService, StandaloneWSResponse, T] {
      override def decode[TT <: RequestLike](response: StandaloneWSResponse, fail: Boolean)(request: TT)(implicit env: PlayService): IO[HttpClientError, T] =
        if (fail && isFailure(response))
          ZIO.fail(HttpResponseError(response.toResponse(request)))
        else
          json__ResponseDecoder.decode(response, fail)(request).flatMap { innerResponse =>
            innerResponse.as[T] match {
              case Right(value) =>
                ZIO.succeed(value)

              case Left(error) =>
                ZIO.fail(HttpParsingError(response.toResponse(request))(error))
            }
          }
    }

  implicit def serviceResponse__ResponseDecoder[T: Decoder]: ResponseDecoder[PlayService, StandaloneWSResponse, ServiceResponse[T]] =
    ???

  implicit val json__ResponseDecoder: ResponseDecoder[PlayService, StandaloneWSResponse, Json] =
    new ResponseDecoder[PlayService, StandaloneWSResponse, Json] {
      override def decode[TT <: RequestLike](response: StandaloneWSResponse, fail: Boolean)(request: TT)(implicit env: PlayService): IO[HttpClientError, Json] =
        if (fail && isFailure(response))
          ZIO.fail(HttpResponseError(response.toResponse(request)))
        else
          ???
    }

  private def isFailure(response: StandaloneWSResponse): Boolean =
    response.status >= 400

}
