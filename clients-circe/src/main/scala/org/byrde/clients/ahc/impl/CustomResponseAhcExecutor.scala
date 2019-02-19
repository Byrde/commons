package org.byrde.clients.ahc.impl

import org.byrde.uri.Path

import play.api.libs.ws.StandaloneWSRequest

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class CustomResponseAhcExecutor extends JsonAhcExecutor {
  self =>

  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit decoder: Decoder[T]): Future[T] =
    super.getJson(path, requestHook, curlRequestHook).map(processResponse[T])(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.postJson(body.asJson)(path, requestHook, curlRequestHook).map(processResponse[TT])(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.putJson(body.asJson)(path, requestHook, curlRequestHook).map(processResponse[TT])(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit decoder: Decoder[T]): Future[T] =
    super.deleteJson(path, requestHook, curlRequestHook).map(processResponse[T])(ec)

  def patch[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.patchJson(body.asJson)(path, requestHook, curlRequestHook).map(processResponse[TT])(ec)

  private def processResponse[T](json: Json)(implicit decoder: Decoder[T]): T =
    json.as[T] match {
      case Right(validated) =>
        validated

      case Left(error) =>
        throw error
    }
}