package org.byrde.http.server.support

import org.byrde.http.server.{ErrorResponse, MaterializedEndpoint, Response}

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future

trait MaterializedEndpointSupport {
  implicit class AuthenticatedEndpoint1[A, U, O](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets, Future]
  ) {
    def toMaterializedEndpoint(
      logic: U => Future[Either[ErrorResponse.Default, O]]
    ): MaterializedEndpoint[A, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter().toRoute(partialServerEndpoint.serverLogic(auth => _ => logic(auth)))
      )
  }
  
  implicit class AuthenticatedEndpoint2[A, U, I, O](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, ErrorResponse.Default, O, AkkaStreams with WebSockets, Future]
  ) {
    def toMaterializedEndpoint(
      logic: (U, I) => Future[Either[ErrorResponse.Default, O]]
    ): MaterializedEndpoint[A, I, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter().toRoute(
          partialServerEndpoint.serverLogic(auth => input => logic(auth, input))
        )
      )
  }
  
  implicit class AuthenticatedEndpoint3[A, U](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets, Future]
  ) {
    def toMaterializedEndpoint(
      logic: U => Future[Either[ErrorResponse.Default, Response.Default]]
    ): MaterializedEndpoint[A, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint1(partialServerEndpoint).toMaterializedEndpoint(logic)
  }
  
  implicit class AuthenticatedEndpoint4[A, U, I](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets, Future]
  ) {
    def toMaterializedEndpoint(
      logic: (U, I) => Future[Either[ErrorResponse.Default, Response.Default]]
    ): MaterializedEndpoint[A, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint2(partialServerEndpoint).toMaterializedEndpoint(logic)
  }
  
  implicit class Endpoint1[O](endpoint: Endpoint[Unit, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(
      logic: => Future[Either[ErrorResponse.Default, O]]
    ): MaterializedEndpoint[Unit, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(endpoint, AkkaHttpServerInterpreter().toRoute(endpoint.serverLogic(_ => logic)))
  }
  
  implicit class Endpoint2[I, O](endpoint: Endpoint[Unit, I, ErrorResponse.Default, O, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(
      logic: I => Future[Either[ErrorResponse.Default, O]]
    ): MaterializedEndpoint[Unit, I, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(endpoint, AkkaHttpServerInterpreter().toRoute(endpoint.serverLogic[Future](logic)))
  }
  
  implicit class Endpoint3(endpoint: Endpoint[Unit, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(
      logic: => Future[Either[ErrorResponse.Default, Response.Default]]
    ): MaterializedEndpoint[Unit, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      Endpoint1(endpoint).toMaterializedEndpoint(logic)
  }
  
  implicit class Endpoint4[I](endpoint: Endpoint[Unit, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(
      logic: I => Future[Either[ErrorResponse.Default, Response.Default]]
    ): MaterializedEndpoint[Unit, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      Endpoint2(endpoint).toMaterializedEndpoint(logic)
  }
}
