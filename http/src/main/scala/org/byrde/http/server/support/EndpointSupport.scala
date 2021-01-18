package org.byrde.http.server.support

import org.byrde.http.server._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.Endpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future

trait EndpointSupport extends CommonEndpointSupport with CodeSupport {
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
