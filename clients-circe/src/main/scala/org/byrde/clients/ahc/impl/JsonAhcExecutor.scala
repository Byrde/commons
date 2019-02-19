package org.byrde.clients.ahc.impl

import org.byrde.uri.Path

import play.api.libs.ws.{BodyWritable, StandaloneWSRequest}

import io.circe.{Encoder, Json}
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future

abstract class JsonAhcExecutor extends BaseAhcExecutor {
  self =>
  import JsonAhcExecutor._

  def getJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[Json] =
    super.underlyingGet(path, requestHook, curlRequestHook).map(_.body).map(parse).map(_.fold(throw _, identity))

  def postJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPost(body.asJson)(path, requestHook, curlRequestHook).map(_.body).map(parse).map(_.fold(throw _, identity))

  def putJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPut(body.asJson)(path, requestHook, curlRequestHook).map(_.body).map(parse).map(_.fold(throw _, identity))

  def deleteJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[Json] =
    super.underlyingDelete(path, requestHook, curlRequestHook).map(_.body).map(parse).map(_.fold(throw _, identity))

  def patchJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPatch(body.asJson)(path, requestHook, curlRequestHook).map(_.body).map(parse).map(_.fold(throw _, identity))
}

object JsonAhcExecutor {
  implicit def circeJsonBodyWriteable: BodyWritable[Json] =
    ???
}
