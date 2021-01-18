package org.byrde.http.server.support

import akka.http.scaladsl.server.Directive1

import org.byrde.http.server.{ErrorResponse, Response, Token}

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.{Endpoint, auth}
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future

trait AunthenticatedEndpointSupport {
  self =>
  
  protected case class AuthenticatedInputEndpoint[I, O, R](
    endpoint: Endpoint[(I, Token), ErrorResponse, O, AkkaStreams with WebSockets]
  )(directive: Token => Directive1[R]) {
    def route(
      logic: I => R => Future[Either[ErrorResponse, O]]
    ): org.byrde.http.server.MaterializedRoute[(I, Token), ErrorResponse, O, AkkaStreams with WebSockets] =
      org.byrde.http.server.MaterializedRoute(
        endpoint,
        AkkaHttpServerInterpreter.toDirective(endpoint).tapply {
          case ((input, token), completion) =>
            directive(token) { dependency =>
              completion(logic(input)(dependency))
            }
        }
      )
  }
  
  protected case class AuthenticatedEndpoint[O, R](
    endpoint: Endpoint[Token, ErrorResponse, O, AkkaStreams with WebSockets]
  )(directive: Token => Directive1[R]) {
    def route(
      logic: R => Future[Either[ErrorResponse, O]]
    ): org.byrde.http.server.MaterializedRoute[Token, ErrorResponse, O, AkkaStreams with WebSockets] =
      org.byrde.http.server.MaterializedRoute(
        endpoint,
        AkkaHttpServerInterpreter.toDirective(endpoint).tapply {
          case (token, completion) =>
            directive(token) { dependency =>
              completion(logic(dependency))
            }
        }
      )
  }
  
  implicit class AuthenticatedEndpoint1[O](endpoint: Endpoint[Unit, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[O, R] =
      AuthenticatedEndpoint(endpoint.in(auth.bearer[String]().map(Token.apply(_))(_.value)))(directive)
  }
  
  implicit class AuthenticatedEndpoint2[I, O](endpoint: Endpoint[I, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedInputEndpoint[I, O, R] =
      AuthenticatedInputEndpoint {
        endpoint.in[Token, (I, Token)](auth.bearer[String]().map(Token.apply(_))(_.value))
      }(directive)
  }
  
  implicit class AuthenticatedEndpoint3(endpoint: Endpoint[Unit, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[Response.Default, R] =
      AuthenticatedEndpoint1(endpoint).bearerAuth(directive)
  }
  
  implicit class AuthenticatedEndpoint4[I](endpoint: Endpoint[I, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedInputEndpoint[I, Response.Default, R] =
      AuthenticatedEndpoint2(endpoint).bearerAuth(directive)
  }
}
