package org.byrde.clients.ahc.impl

import org.byrde.uri.Path

import play.api.libs.json._
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.Future

abstract class JsonAhcExecutor extends BaseAhcExecutor {
  self =>
  def getJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[JsValue] =
    super.underlyingGet(path, requestHook, curlRequestHook).map(_.body).map(Json.parse)

  def postJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit writes: Writes[T]): Future[JsValue] =
    super.underlyingPost(Json.toJson(body))(path, requestHook, curlRequestHook).map(_.body).map(Json.parse)

  def putJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit writes: Writes[T]): Future[JsValue] =
    super.underlyingPut(Json.toJson(body))(path, requestHook, curlRequestHook).map(_.body).map(Json.parse)

  def deleteJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[JsValue] =
    super.underlyingDelete(path, requestHook, curlRequestHook).map(_.body).map(Json.parse)
}

object JsonAhcExecutor {
  case class JsParsingError(res: JsValue, err: JsError)
}
