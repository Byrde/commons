package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.BoxedServiceResponseException
import org.byrde.uri.{Host, Path}

import akka.util.ByteString

import play.api.libs.ws.{BodyWritable, InMemoryBody, StandaloneWSRequest, StandaloneWSResponse}

import com.github.ghik.silencer.silent

import io.circe.parser.parse
import io.circe.{Decoder, Encoder, Json, Printer}
import io.circe.syntax._

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class CustomResponseAhcExecutor extends BaseAhcExecutor {
  self =>

  import CustomResponseAhcExecutor._

  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[T] =
    super.underlyingGet(path, requestHook).map(processResponse[T]("GET", path.toString))(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.underlyingPost(body.asJson)(path, requestHook).map(processResponse[TT]("POST", path.toString))(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.underlyingPut(body.asJson)(path, requestHook).map(processResponse[TT]("PUT", path.toString))(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit decoder: Decoder[T]): Future[T] =
    super.underlyingDelete(path, requestHook).map(processResponse[T]("DELETE", path.toString))(ec)

  def patch[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit encoder: Encoder[T], decoder: Decoder[TT]): Future[TT] =
    super.underlyingPatch(body.asJson)(path, requestHook).map(processResponse[TT]("PATCH", path.toString))(ec)

  private def processResponse[T: ClassTag](method: String, path: String)(standaloneWSResponse: StandaloneWSResponse)(implicit decoder: Decoder[T]): T =
    parse(standaloneWSResponse.errorHook(method, host, path).body).flatMap(_.as[T]) match {
      case Right(validated) =>
        validated

      case Left(exception) =>
        throw BoxedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(exception)
    }
}

object CustomResponseAhcExecutor {
  private val Error = 400

  private val defaultPrinter =
    Printer.noSpaces

  private case class CustomResponseException(response: String) extends Exception(response)

  implicit def circeJsonBodyWriteable(implicit printer: Printer = defaultPrinter): BodyWritable[Json] =
    BodyWritable(
      json => InMemoryBody(ByteString.fromString(json.pretty(printer))),
      "application/json"
    )

  implicit class StandaloneWSResponse2ServiceResponseError(value: StandaloneWSResponse) {
    @silent @inline def errorHook[T: ClassTag](method: String, host: Host, path: String): StandaloneWSResponse =
      if (value.status >= Error)
        throw BoxedServiceResponseException(host.protocol.toString, host.host, host.port.map(_.toString), method, path)(CustomResponseException(value.body))
      else
        value
  }
}