package org.byrde.clients.ahc.impl

import org.byrde.uri.Path

import akka.util.ByteString

import play.api.libs.ws.{BodyWritable, InMemoryBody, StandaloneWSRequest}

import io.circe.{Encoder, Json, Printer}
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
  private val defaultPrinter =
    Printer.noSpaces

  implicit def circeJsonBodyWriteable(implicit printer: Printer = defaultPrinter): BodyWritable[Json] =
    BodyWritable(
      json => InMemoryBody(ByteString.fromString(json.pretty(printer))),
      "application/json")
}
