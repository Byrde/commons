package org.byrde.http.server.support

import org.byrde.http.server._
import org.byrde.uri.{Host, Url}

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.client3.{SttpBackend, UriContext}
import sttp.model.Uri
import sttp.tapir._
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.server.PartialServerEndpoint

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._

trait ProxyMaterializedEndpointSupport extends MaterializedEndpointSupport {
  type Identity[X] = X
  
  implicit class AuthenticatedProxyEndpoint1[A, U, E, O](
    partialEndpoint: PartialServerEndpoint[A, U, Unit, E, O, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Some(uri"$host")
        )
        .pipe { fn =>
          partialEndpoint.toMaterializedEndpoint { _ =>
            fn(())
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
    
    def toProxyMaterializedEndpoint(
      url: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Option.empty
        )
        .pipe { fn =>
          partialEndpoint.toMaterializedEndpoint { _ =>
            fn(())
              .copy(uri = uri"${url.toString}": Identity[Uri])
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class AuthenticatedProxyEndpoint2[A, U, I, E, O, R](
    partialEndpoint: PartialServerEndpoint[A, U, I, E, O, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Some(uri"$host")
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
    
    def toProxyMaterializedEndpoint(
      url: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          partialEndpoint
            .endpoint
            .copy(securityInput = EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)),
          Option.empty
        )
        .pipe { fn =>
          partialEndpoint.toMaterializedEndpoint {
            case (_, input) =>
              fn(input)
                .copy(uri = uri"$url": Identity[Uri])
                .headers(headers)
                .send(backend)
                .map(_.body)
          }
        }
  }
  
  implicit class AuthenticatedProxyEndpoint3[A, U, E](
    partialServerEndpoint: PartialServerEndpoint[A, U, Unit, E, Ack, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint1(partialServerEndpoint).toProxyMaterializedEndpoint(host, headers)
    
    def toProxyMaterializedEndpoint(
      uri: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, Unit, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint1(partialServerEndpoint).toProxyMaterializedEndpoint(uri, headers)
  }
  
  implicit class AuthenticatedProxyEndpoint4[A, U, I, E](
    partialServerEndpoint: PartialServerEndpoint[A, U, I, E, Ack, AkkaStreams with WebSockets, Future]
  ) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint2(partialServerEndpoint).toProxyMaterializedEndpoint(host, headers)
    
    def toProxyMaterializedEndpoint(
      url: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[A, I, E, Ack, AkkaStreams with WebSockets] =
      AuthenticatedProxyEndpoint2(partialServerEndpoint).toProxyMaterializedEndpoint(url, headers)
  }
  
  implicit class ProxyEndpoint1[E, O](endpoint: Endpoint[Unit, Unit, E, O, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Some(uri"$host")
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint {
            fn(())
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
    
    def toProxyMaterializedEndpoint(
      url: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Option.empty
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint {
            fn(())
              .copy(uri = uri"$url": Identity[Uri])
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class ProxyEndpoint2[I, E, O](endpoint: Endpoint[Unit, I, E, O, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Some(uri"$host")
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint { input =>
            fn(input)
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
    
    def toProxyMaterializedEndpoint(
      url: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, E, O, AkkaStreams with WebSockets] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(
          endpoint,
          Option.empty
        )
        .pipe { fn =>
          endpoint.toMaterializedEndpoint { input =>
            fn(input)
              .copy(uri = uri"${url.toString}": Identity[Uri])
              .headers(headers)
              .send(backend)
              .map(_.body)
          }
        }
  }
  
  implicit class ProxyEndpoint3[E](endpoint: Endpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets] =
      ProxyEndpoint1(endpoint).toProxyMaterializedEndpoint(host, headers)
    
    def toProxyMaterializedEndpoint(
      uri: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, Unit, E, Ack, AkkaStreams with WebSockets] =
      ProxyEndpoint1(endpoint).toProxyMaterializedEndpoint(uri, headers)
  }
  
  implicit class ProxyEndpoint4[I, E](endpoint: Endpoint[Unit, I, E, Ack, AkkaStreams with WebSockets]) {
    def toProxyMaterializedEndpoint(
      host: Host,
      headers: Map[String, String],
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, E, Ack, AkkaStreams with WebSockets] =
      ProxyEndpoint2(endpoint).toProxyMaterializedEndpoint(host, headers)
    
    def toProxyMaterializedEndpoint(
      uri: Url,
      headers: Map[String, String] = Map.empty,
    )(implicit backend: SttpBackend[Future, AkkaStreams with WebSockets]): MaterializedEndpoint[Unit, I, E, Ack, AkkaStreams with WebSockets] =
      ProxyEndpoint2(endpoint).toProxyMaterializedEndpoint(uri, headers)
  }
}
