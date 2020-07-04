package org.byrde.client.http.play

import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, StandaloneWSResponse}
import play.api.mvc

import org.byrde.client.http._
import org.byrde.client.http.HttpClientError.{HttpDecodingError, HttpParsingError, HttpResponseError, HttpServiceResponseError}
import org.byrde.client.http.play.support.{BodyWritableSupport, ProxyRequestSupport}
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.{Message, ServiceResponse, ServiceResponseType}
import org.byrde.uri.{Path, Url}

import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{Decoder, Json}

package object implicits extends BodyWritableSupport with ProxyRequestSupport {

  implicit def proxy__RequestEncoder[T: BodyWritable]: RequestEncoder[PlayService, mvc.Request[T], StandaloneWSRequest] =
    new RequestEncoder[PlayService, mvc.Request[T], StandaloneWSRequest] {
      override def encode(request: mvc.Request[T])(implicit env: PlayService): StandaloneWSRequest =
        request.toWSRequest(env.url(Url(env.host, Path.fromString(request.path)).toString))
    }
  
  implicit def unit__RequestEncoder: RequestEncoder[PlayService, Request[Unit], StandaloneWSRequest] =
    new RequestEncoder[PlayService, Request[Unit], StandaloneWSRequest] {
      override def encode(request: Request[Unit])(implicit env: PlayService): StandaloneWSRequest =
        _request(request)
    }
  
  implicit def generic__RequestEncoder[T: BodyWritable]: RequestEncoder[PlayService, Request[T], StandaloneWSRequest] =
    new RequestEncoder[PlayService, Request[T], StandaloneWSRequest] {
      override def encode(request: Request[T])(implicit env: PlayService): StandaloneWSRequest =
        _request(request).withBody(request.body)
    }

  implicit val json__RequestEncoder: RequestEncoder[PlayService, Request[Json], StandaloneWSRequest] =
    new RequestEncoder[PlayService, Request[Json], StandaloneWSRequest] {
      override def encode(request: Request[Json])(implicit env: PlayService): StandaloneWSRequest =
        _request(request).withBody(request.body)
    }

  implicit def generic__ResponseDecoder[T: Decoder]: ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, T] =
    new ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, T] {
      override def decode(request: StandaloneWSRequest)(response: StandaloneWSResponse)(
        implicit env: PlayService
      ): Either[HttpClientError, T] =
        if (isFailure(response))
          Left(HttpResponseError(incompleteResponse(request)(response)))
        else
          json__ResponseDecoder.decode(request)(response).flatMap { innerResponse =>
            innerResponse.as[T] match {
              case Right(value) =>
                Right(value)

              case Left(error) =>
                Left(HttpDecodingError(incompleteResponse(request)(response))(error))
            }
          }
    }

  implicit def serviceResponse__ResponseDecoder[T: Decoder]: ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, ServiceResponse[T]] =
    new ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, ServiceResponse[T]] {
      override def decode(request: StandaloneWSRequest)(
        response: StandaloneWSResponse,
      )(implicit env: PlayService): Either[HttpClientError, ServiceResponse[T]] =
        if (isFailure(response))
          Left(HttpResponseError(incompleteResponse(request)(response)))
        else
          json__ResponseDecoder.decode(request)(response)
            .flatMap { innerResponse =>
              innerResponse.as[TransientServiceResponse[Option[Message]]] match {
                case Right(validated) if validated.`type` == ServiceResponseType.Error =>
                  Left(HttpServiceResponseError(incompleteResponse(request)(response))(validated.code))

                case _ =>
                  Right(innerResponse)
              }
            }
            .flatMap { innerResponse =>
              innerResponse.as[TransientServiceResponse[T]]
                .left
                .map(HttpDecodingError(incompleteResponse(request)(response)))
            }
    }

  implicit val json__ResponseDecoder: ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, Json] =
    new ResponseDecoder[PlayService, StandaloneWSRequest, StandaloneWSResponse, Json] {
      override def decode(request: StandaloneWSRequest)(response: StandaloneWSResponse)(
        implicit env: PlayService
      ): Either[HttpClientError, Json] =
        if (isFailure(response))
          Left(HttpResponseError(incompleteResponse(request)(response)))
        else
          parse(response.body) match {
            case Right(response) =>
              Right(response)

            case Left(error) =>
              Left(HttpParsingError(incompleteResponse(request)(response))(error))
          }
    }

  private def _request(request: Request[_])(implicit env: PlayService) =
    env
      .url(Url(env.host, request.path).toString)
      .withMethod(request.method)
      .withHttpHeaders(request.headers.toSeq: _*)

  private def isFailure(response: StandaloneWSResponse): Boolean =
    response.status >= 400
  
  private def incompleteResponse(request: StandaloneWSRequest)(response: StandaloneWSResponse)(implicit env: PlayService) =
    Response(
      Url(env.host, Path.fromString(request.uri.getPath)),
      request.method,
      request.headers.view.mapValues(_.mkString(", ")).toMap,
      response.headers.view.mapValues(_.mkString(", ")).toMap,
      response.status,
      response.body
    )

}
