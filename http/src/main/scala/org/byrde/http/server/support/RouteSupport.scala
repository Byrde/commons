package org.byrde.http.server.support

import akka.http.scaladsl.server._

import org.byrde.http.server._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.server.akkahttp._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.ChainingSyntax

trait RouteSupport extends RequestSupport with ResponseSupport with WithSuccessAndErrorCode with RequestIdSupport with ChainingSyntax {
  private lazy val bearerAuthMatcher =
    auth.bearer[String].map(Token.apply(_))(_.value)
  
  implicit def options: AkkaHttpServerOptions
  
  implicit def route2Routes(route: org.byrde.http.server.Route[_, _, _, _]): Routes =
    Routes(route)
  
  implicit class ChainRoute(route: org.byrde.http.server.Route[_, _, _, _]) {
    def ~ (_route: org.byrde.http.server.Route[_, _, _, _]): Routes =
      Routes(Seq(route, _route))
  }
  
  implicit class ChainRoutes(routes: Routes) {
    def ~ (route: org.byrde.http.server.Route[_, _, _, _]): Routes =
      Routes(routes.value :+ route)
  }
  
  implicit class RichUnitEndpoint[T](endpoint: Endpoint[Unit, ErrorResponse, T, AkkaStreams with WebSockets]) {
    protected class AuthenticatedEndpoint[R](endpoint: Endpoint[Token, ErrorResponse, T, AkkaStreams with WebSockets])(directive: Token => Directive1[R]) {
      def toRoute(
        logic: R => Future[Either[ErrorResponse, T]]
      ): org.byrde.http.server.Route[Token, ErrorResponse, T, AkkaStreams with WebSockets] =
        org.byrde.http.server.Route(
          endpoint,
          sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
            case (token, completion) =>
              directive(token) { dependency =>
                completion(logic(dependency))
              }
          }
        )
    }
  
    def auth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[R] =
      new AuthenticatedEndpoint(endpoint.in(bearerAuthMatcher))(directive)
    
    def toRoute(logic: () => Future[Either[ErrorResponse, T]]): org.byrde.http.server.Route[Unit, ErrorResponse, T, AkkaStreams with WebSockets] =
      org.byrde.http.server.Route(endpoint, sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(_ => logic()))
  
    def toAuthenticatedRoute[R](
      directive: Token => Directive1[R]
    )(logic: R => Future[Either[ErrorResponse, T]]): org.byrde.http.server.Route[Token, ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint.auth(directive).toRoute(logic)
  }
  
  implicit class RichEndpoint[I, T](endpoint: Endpoint[I, ErrorResponse, T, AkkaStreams with WebSockets]) {
    protected class AuthenticatedEndpoint[R](endpoint: Endpoint[(I, Token), ErrorResponse, T, AkkaStreams with WebSockets])(directive: Token => Directive1[R]) {
      def toRoute(
        logic: I => R => Future[Either[ErrorResponse, T]]
      ): org.byrde.http.server.Route[(I, Token), ErrorResponse, T, AkkaStreams with WebSockets] =
        org.byrde.http.server.Route(
          endpoint,
          sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
            case ((input, token), completion) =>
              directive(token) { dependency =>
                completion(logic(input)(dependency))
              }
          }
        )
    }
  
    def auth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[R] =
      new AuthenticatedEndpoint(endpoint.in(bearerAuthMatcher))(directive)
    
    def toRoute(logic: I => Future[Either[ErrorResponse, T]]): org.byrde.http.server.Route[I, ErrorResponse, T, AkkaStreams with WebSockets] =
      org.byrde.http.server.Route(endpoint, sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(logic))
    
    def toAuthenticatedRoute[R](
      directive: Token => Directive1[R]
    )(logic: I => R => Future[Either[ErrorResponse, T]]): org.byrde.http.server.Route[(I, Token), ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint.auth(directive).toRoute(logic)
  }
}
