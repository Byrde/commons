package org.byrde.http.server.support

import org.byrde.http.server._

import io.circe.generic.auto._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.{Endpoint, EndpointIO, EndpointOutput, statusMappingValueMatcher}

import scala.concurrent.Future

trait EndpointSupport {
  self: CodeSupport =>
  
  private lazy val defaultMatcher: EndpointOutput.StatusMapping[ErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Response.Default("Error", errorCode))
    ) {
      case err: ErrorResponse if err.code == errorCode => true
    }
  
  lazy val defaultMapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    sttp.tapir.oneOf[ErrorResponse](defaultMatcher)
  
  lazy val ackOutput: EndpointIO.Body[String, Response.Default] =
    jsonBody[Response.Default]
      .description(s"Default response! Success code: $successCode")
      .example(Response.Default("Success", successCode))
  
  implicit class Endpoint1[O](endpoint: Endpoint[Unit, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def route(
      logic: () => Future[Either[ErrorResponse, O]]
    ): org.byrde.http.server.MaterializedRoute[Unit, ErrorResponse, O, AkkaStreams with WebSockets] =
      org.byrde.http.server.MaterializedRoute(
        endpoint,
        AkkaHttpServerInterpreter.toRoute(endpoint)(_ => logic())
      )
  }
  
  implicit class Endpoint2[I, O](endpoint: Endpoint[I, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def route(
      logic: I => Future[Either[ErrorResponse, O]]
    ): org.byrde.http.server.MaterializedRoute[I, ErrorResponse, O, AkkaStreams with WebSockets] =
      org.byrde.http.server.MaterializedRoute(
        endpoint,
        AkkaHttpServerInterpreter.toRoute(endpoint)(logic)
      )
  }
  
  implicit class Endpoint3(endpoint: Endpoint[Unit, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def route(
      logic: () => Future[Either[ErrorResponse, Response.Default]]
    ): org.byrde.http.server.MaterializedRoute[Unit, ErrorResponse, Response.Default, AkkaStreams with WebSockets] =
      Endpoint1(endpoint).route(logic)
  }
  
  implicit class Endpoint4[I](endpoint: Endpoint[I, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def route(
      logic: I => Future[Either[ErrorResponse, Response.Default]]
    ): org.byrde.http.server.MaterializedRoute[I, ErrorResponse, Response.Default, AkkaStreams with WebSockets] =
      Endpoint2(endpoint).route(logic)
  }
}
