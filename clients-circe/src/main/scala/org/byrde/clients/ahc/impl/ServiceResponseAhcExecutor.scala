package org.byrde.clients.ahc.impl

import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.exceptions.{BoxedResponseException, BoxedServiceResponseException}
import org.byrde.service.response.{Message, ServiceResponseType}
import org.byrde.service.response.utils.ServiceResponseUtils._
import org.byrde.uri.{Host, Path}

import com.github.ghik.silencer.silent

import play.api.libs.ws.StandaloneWSRequest
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.Future

abstract class ServiceResponseAhcExecutor extends JsonAhcExecutor {
  self =>
  import ServiceResponseAhcExecutor._

  @silent def get[T](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[TransientServiceResponse[T]] =
    getEither(path, requestHook).map(_.fold(throw _, identity))

  @silent def getEither[T](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[Either[BoxedServiceResponseException, TransientServiceResponse[T]]] =
    super.getJson(path, requestHook).map(processResponse[T]("GET", path.toString))(ec)

  @silent def post[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    postEither(body)(path, requestHook).map(_.fold(throw _, identity))

  @silent def postEither[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[Either[BoxedServiceResponseException, TransientServiceResponse[TT]]] =
    super.postJson(body)(path, requestHook).map(processResponse[TT]("POST", path.toString))(ec)

  @silent def put[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    putEither(body)(path, requestHook).map(_.fold(throw _, identity))

  @silent def putEither[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[Either[BoxedServiceResponseException, TransientServiceResponse[TT]]] =
    super.putJson(body)(path, requestHook).map(processResponse[TT]("PUT", path.toString))(ec)

  @silent def delete[T](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[TransientServiceResponse[T]] =
    deleteEither(path, requestHook).map(_.fold(throw _, identity))

  @silent def deleteEither[T](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[Either[BoxedServiceResponseException, TransientServiceResponse[T]]] =
    super.deleteJson(path, requestHook).map(processResponse[T]("DELETE", path.toString))(ec)

  @silent def patch[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    patchEither(body)(path, requestHook).map(_.fold(throw _, identity))

  @silent def patchEither[T, TT](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[Either[BoxedServiceResponseException, TransientServiceResponse[TT]]] =
    super.patchJson(body)(path, requestHook).map(processResponse[TT]("PATCH", path.toString))(ec)

  private def processResponse[T](method: String, path: String)(json: Json)(implicit decoder: Decoder[TransientServiceResponse[T]]): Either[BoxedServiceResponseException, TransientServiceResponse[T]] =
    json
      .errorHook(method, host, path)
      .flatMap { json =>
        json.as[TransientServiceResponse[T]] match {
          case Right(validated: TransientServiceResponse[T]) =>
            Right(validated)

          case Left(exception) =>
            throw new BoxedResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(exception)
        }
      }
}

object ServiceResponseAhcExecutor {
  implicit class JsValue2ServiceResponseError(value: Json) {
    @silent @inline def errorHook(method: String, host: Host, path: String)(implicit decoder: Decoder[Option[Message]]): Either[BoxedServiceResponseException, Json] =
      value
        .as[TransientServiceResponse[Option[Message]]] match {
          case Right(validated) if validated.`type` == ServiceResponseType.Error =>
            Left(new BoxedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(validated.toException("BoxedServiceResponseException")))

          case _ =>
            Right(value)
        }
  }
}
