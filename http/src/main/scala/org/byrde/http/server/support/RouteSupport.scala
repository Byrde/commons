package org.byrde.http.server.support

import akka.http.scaladsl.server.Directive1

import org.byrde.http.server._

import io.circe.generic.auto._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server._
import sttp.tapir.server.akkahttp.AkkaHttpServerOptions
import sttp.tapir.{Endpoint, auth, statusCode}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.ChainingSyntax

trait RouteSupport extends RequestSupport with ResponseSupport with WithSuccessAndErrorCode with RequestIdSupport with ChainingSyntax {
  private lazy val bearerAuthMatcher =
    auth.bearer[String].map(Token.apply(_))(_.value)
  
  private implicit lazy val options: AkkaHttpServerOptions =
    AkkaHttpServerOptions.default.copy(decodeFailureHandler = decodeFailureHandler)
  
  implicit def route2Routes(route: Route[_, _, _, _]): Seq[Route[_, _, _, _]] =
    Seq(route)
  
  implicit class RichUnitEndpoint[T](endpoint: Endpoint[Unit, ErrorResponse, T, AkkaStreams with WebSockets]) {
    protected class AuthenticatedEndpoint[R](
      endpoint: Endpoint[Token, ErrorResponse, T, AkkaStreams with WebSockets]
    )(directive: Token => Directive1[R]) {
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
  
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[R] =
      new AuthenticatedEndpoint(endpoint.in(bearerAuthMatcher))(directive)
    
    def toRoute(
      logic: () => Future[Either[ErrorResponse, T]]
    ): org.byrde.http.server.Route[Unit, ErrorResponse, T, AkkaStreams with WebSockets] =
      org.byrde.http.server.Route(
        endpoint,
        sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(_ => logic())
      )
  
    def toAuthenticatedRoute[R](
      directive: Token => Directive1[R]
    )(
      logic: R => Future[Either[ErrorResponse, T]]
    ): org.byrde.http.server.Route[Token, ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint.bearerAuth(directive).toRoute(logic)
  }
  
  implicit class RichEndpoint[I, T](endpoint: Endpoint[I, ErrorResponse, T, AkkaStreams with WebSockets]) {
    protected class AuthenticatedEndpoint[R](
      endpoint: Endpoint[(I, Token), ErrorResponse, T, AkkaStreams with WebSockets]
    )(directive: Token => Directive1[R]) {
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
  
    def bearerAuth[R](directive: Token => Directive1[R]): AuthenticatedEndpoint[R] =
      new AuthenticatedEndpoint(endpoint.in(bearerAuthMatcher))(directive)
    
    def toRoute(
      logic: I => Future[Either[ErrorResponse, T]]
    ): org.byrde.http.server.Route[I, ErrorResponse, T, AkkaStreams with WebSockets] =
      org.byrde.http.server.Route(
        endpoint,
        sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(logic)
      )
    
    def toAuthenticatedRoute[R](
      directive: Token => Directive1[R]
    )(
      logic: I => R => Future[Either[ErrorResponse, T]]
    ): org.byrde.http.server.Route[(I, Token), ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint.bearerAuth(directive).toRoute(logic)
  }
  
  private def decodeFailureHandler: DefaultDecodeFailureHandler =
    ServerDefaults.decodeFailureHandler.copy(response = handleDecodeFailure)
  
  private def handleDecodeFailure(code: StatusCode, message: String): DecodeFailureHandling =
    (code, Response.Default(message, errorCode))
      .pipe(DecodeFailureHandling.response(statusCode.and(jsonBody[ErrorResponse]))(_))
}
