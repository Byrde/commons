package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.BoxedServiceResponseException
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.{Message, ServiceResponseType}
import org.byrde.service.response.utils.ServiceResponseUtils._
import org.byrde.uri.{Host, Path}

import com.github.ghik.silencer.silent

import play.api.libs.ws.StandaloneWSRequest
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class ServiceResponseAhcExecutor extends JsonAhcExecutor {
  self =>
  import ServiceResponseAhcExecutor._

  @silent def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[TransientServiceResponse[T]] =
    super.getJson(path, requestHook).map(processResponse[T]("GET", path.toString)(_))(ec)

  @silent def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    super.postJson(body)(path, requestHook).map(processResponse[TT]("POST", path.toString))(ec)

  @silent def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    super.putJson(body)(path, requestHook).map(processResponse[TT]("PUT", path.toString))(ec)

  @silent def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[TransientServiceResponse[T]] =
    super.deleteJson(path, requestHook).map(processResponse[T]("DELETE", path.toString)(_))(ec)

  @silent def patch[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TransientServiceResponse[TT]] =
    super.patchJson(body)(path, requestHook).map(processResponse[TT]("PATCH", path.toString))(ec)

  private def processResponse[T: ClassTag](method: String, path: String)(json: Json)(implicit decoder: Decoder[TransientServiceResponse[T]]): TransientServiceResponse[T] =
    json
      .errorHook(method, host, path)
      .as[TransientServiceResponse[T]] match {
        case Right(validated: TransientServiceResponse[T]) =>
          validated

        case Left(exception) =>
          throw BoxedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(exception)
      }
}

object ServiceResponseAhcExecutor {
  implicit class JsValue2ServiceResponseError(value: Json) {
    @silent @inline def errorHook[T: ClassTag](method: String, host: Host, path: String)(implicit decoder: Decoder[Option[Message]]): Json =
      value
        .as[TransientServiceResponse[Option[Message]]] match {
          case Right(validated) if validated.`type` == ServiceResponseType.Error =>
            throw BoxedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(validated.toException("BoxedServiceResponseException"))

          case _ =>
            value
        }
  }
}
