package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.BoxedTypedServiceResponseException
import org.byrde.uri.Path

import play.api.libs.ws.StandaloneWSRequest

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class CustomResponseAhcExecutor extends JsonAhcExecutor {
  self =>

  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[T] =
    super.getJson(path, requestHook).map(processResponse[T]("GET", path.toString))(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.postJson(body.asJson)(path, requestHook).map(processResponse[TT]("POST", path.toString))(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.putJson(body.asJson)(path, requestHook).map(processResponse[TT]("PUT", path.toString))(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[T] =
    super.deleteJson(path, requestHook).map(processResponse[T]("DELETE", path.toString))(ec)

  def patch[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.patchJson(body.asJson)(path, requestHook).map(processResponse[TT]("PATCH", path.toString))(ec)

  private def processResponse[T: ClassTag](method: String, path: String)(json: Json)(implicit decoder: Decoder[T]): T =
    json.as[T] match {
      case Right(validated) =>
        validated

      case Left(exception) =>
        throw BoxedTypedServiceResponseException[T](host.protocol.toString, host.host, host.port.map(_.toString), method, path)(exception)
    }
}