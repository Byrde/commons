package org.byrde.http.server.support

import org.byrde.http.server._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.client3.SttpBackend
import sttp.model.Uri
import sttp.tapir._
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.server.PartialServerEndpoint

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._

trait ProxyMaterializedEndpointSupport extends MaterializedEndpointSupport {
  implicit class AuthenticatedProxyEndpoint1[A, U, O](
    partialEndpoint: PartialServerEndpoint[A, U, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Some(uri)
        )
        .pipe { fn =>
          partialEndpoint.toMaterializedEndpoint { _ =>
            fn(())
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class AuthenticatedProxyEndpoint2[A, U, I, O, R](
    partialEndpoint: PartialServerEndpoint[A, U, I, ErrorResponse.Default, O, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Some(uri)
        )
        .pipe { fn =>
          partialEndpoint.toMaterializedEndpoint {
            case (_, input) =>
              fn(input)
                .headers(headers)
                .send(backend)
                .map(_.body)
          }
        }
  }
  
  implicit class AuthenticatedProxyEndpoint3[A, U](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint1(partialServerEndpoint).toProxyMaterializedEndpoint(uri, headers)
  }
  
  implicit class AuthenticatedProxyEndpoint4[A, U, I](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint2(partialServerEndpoint).toProxyMaterializedEndpoint(uri, headers)
  }
  
  implicit class ProxyEndpoint1[O](endpoint: Endpoint[Unit, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Some(uri)
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint {
            fn(())
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class ProxyEndpoint2[I, O](endpoint: Endpoint[Unit, I, ErrorResponse.Default, O, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, ErrorResponse.Default, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Some(uri)
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint { input =>
            fn(input)
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class ProxyEndpoint3(endpoint: Endpoint[Unit, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      ProxyEndpoint1(endpoint).toProxyMaterializedEndpoint(uri, headers)
  }
  
  implicit class ProxyEndpoint4[I](endpoint: Endpoint[Unit, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, ErrorResponse.Default, Response.Default, AkkaStreams with WebSockets] =
      ProxyEndpoint2(endpoint).toProxyMaterializedEndpoint(uri, headers)
  }
}
