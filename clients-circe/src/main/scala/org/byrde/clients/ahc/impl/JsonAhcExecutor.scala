package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.BoxedTypedServiceResponseException
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

  def getJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity): Future[Json] =
    super.underlyingGet(path, requestHook).map(_.body).map(processResponse("GET", path.toString))

  def postJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPost(body.asJson)(path, requestHook).map(_.body).map(processResponse("POST", path.toString))

  def putJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPut(body.asJson)(path, requestHook).map(_.body).map(processResponse("PUT", path.toString))

  def deleteJson(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity): Future[Json] =
    super.underlyingDelete(path, requestHook).map(_.body).map(processResponse("DELETE", path.toString))

  def patchJson[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T]): Future[Json] =
    super.underlyingPatch(body.asJson)(path, requestHook).map(_.body).map(processResponse("PATCH", path.toString))

  private def processResponse(method: String, path: String)(body: String): Json =
    parse(body) match {
      case Right(response) =>
        response

      case Left(exception) =>
        throw BoxedTypedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(exception)
    }
}

object JsonAhcExecutor {
  private val defaultPrinter =
    Printer.noSpaces

  implicit def circeJsonBodyWriteable(implicit printer: Printer = defaultPrinter): BodyWritable[Json] =
    BodyWritable(
      json => InMemoryBody(ByteString.fromString(json.pretty(printer))),
      "application/json"
    )
}
