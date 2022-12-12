package org.byrde.http.server.support

import org.byrde.http.server.{ Ack, MaterializedEndpoint }

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.akkahttp.{ AkkaHttpServerInterpreter, AkkaHttpServerOptions }

import scala.concurrent.{ ExecutionContext, Future }

trait MaterializedEndpointSupport {
  implicit class AuthenticatedEndpoint1[A, U, E, O](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, E, O, AkkaStreams with WebSockets, Future],
  ) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: U => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, Unit, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter(serverOptions).toRoute(partialServerEndpoint.serverLogic(auth => _ => logic(auth))),
      )

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: U => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, Unit, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter().toRoute(partialServerEndpoint.serverLogic(auth => _ => logic(auth))),
      )
  }

  implicit class AuthenticatedEndpoint2[A, U, I, E, O](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, E, O, AkkaStreams with WebSockets, Future],
  ) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: (U, I) => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, I, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter(serverOptions).toRoute(
          partialServerEndpoint.serverLogic(auth => input => logic(auth, input)),
        ),
      )

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: (U, I) => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, I, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        partialServerEndpoint.endpoint,
        AkkaHttpServerInterpreter().toRoute(
          partialServerEndpoint.serverLogic(auth => input => logic(auth, input)),
        ),
      )
  }

  implicit class AuthenticatedEndpoint3[A, U, E](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, E, Ack, AkkaStreams with WebSockets, Future],
  ) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: U => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, Unit, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint1(partialServerEndpoint).toMaterializedEndpoint(serverOptions, hide)(logic)

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: U => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, Unit, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint1(partialServerEndpoint).toMaterializedEndpoint(hide)(logic)
  }

  implicit class AuthenticatedEndpoint4[A, U, I, E](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, E, Ack, AkkaStreams with WebSockets, Future],
  ) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: (U, I) => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, I, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint2(partialServerEndpoint).toMaterializedEndpoint(serverOptions, hide)(logic)

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: (U, I) => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[A, I, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedEndpoint2(partialServerEndpoint).toMaterializedEndpoint(hide)(logic)
  }

  implicit class Endpoint1[E, O](endpoint: Endpoint[Unit, Unit, E, O, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: =>Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, Unit, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        endpoint,
        AkkaHttpServerInterpreter(serverOptions).toRoute(endpoint.serverLogic(_ => logic)),
      )

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: =>Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, Unit, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(hide, endpoint, AkkaHttpServerInterpreter().toRoute(endpoint.serverLogic(_ => logic)))
  }

  implicit class Endpoint2[I, E, O](endpoint: Endpoint[Unit, I, E, O, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: I => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, I, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(
        hide,
        endpoint,
        AkkaHttpServerInterpreter(serverOptions).toRoute(endpoint.serverLogic[Future](logic)),
      )

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: I => Future[Either[E, O]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, I, E, O, AkkaStreams with WebSockets] =
      MaterializedEndpoint(hide, endpoint, AkkaHttpServerInterpreter().toRoute(endpoint.serverLogic[Future](logic)))
  }

  implicit class Endpoint3[E](endpoint: Endpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: =>Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets] =
      Endpoint1(endpoint).toMaterializedEndpoint(serverOptions, hide)(logic)

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: =>Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets] =
      Endpoint1(endpoint).toMaterializedEndpoint(hide)(logic)
  }

  implicit class Endpoint4[I, E](endpoint: Endpoint[Unit, I, E, Ack, AkkaStreams with WebSockets]) {
    def toMaterializedEndpoint(serverOptions: AkkaHttpServerOptions, hide: Boolean)(
      logic: I => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, I, E, Ack, AkkaStreams with WebSockets] =
      Endpoint2(endpoint).toMaterializedEndpoint(serverOptions, hide)(logic)

    def toMaterializedEndpoint(hide: Boolean = false)(
      logic: I => Future[Either[E, Ack]],
    )(implicit ec: ExecutionContext): MaterializedEndpoint[Unit, I, E, Ack, AkkaStreams with WebSockets] =
      Endpoint2(endpoint).toMaterializedEndpoint(hide)(logic)
  }
}
