package org.byrde.http.server.support

import akka.http.scaladsl.server._

import org.byrde.http.server.{Response, Route, Routes, _}

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.server.akkahttp._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.ChainingSyntax

trait RouteSupport extends RequestSupport with ResponseSupport with WithSuccessAndErrorCode with RequestIdSupport with ChainingSyntax {
  implicit def options: AkkaHttpServerOptions
  
  implicit def route2Routes(route: Route[_, _, _, _]): Routes =
    Routes(route)
  
  implicit class ChainTapirRoute(route: Route[_, _, _, _]) {
    def ~ (_route: Route[_, _, _, _]): Routes =
      Routes(Seq(route, _route))
  }
  
  implicit class ChainTapirRoutes(routes: Routes) {
    def ~ (route: Route[_, _, _, _]): Routes =
      Routes(routes.value :+ route)
  }
  
  implicit class RichUnitEndpoint[T](endpoint: Endpoint[Unit, ErrorResponse, T, AkkaStreams with WebSockets]) {
    def toRoute(logic: () => Future[Either[ErrorResponse, T]]): Route[Unit, ErrorResponse, T, AkkaStreams with WebSockets] =
      Route(endpoint, sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(_ => logic()))
    
    def toRouteWithDirective[R](directive: Directive1[R])(
      logic: R => Future[Either[ErrorResponse, T]]
    ): Route[Unit, ErrorResponse, T, AkkaStreams with WebSockets] =
      Route(
        endpoint,
        directive { dependency =>
          sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
            case (_, completion) =>
              completion(logic(dependency))
          }
        }
      )
  
    def toRouteWithAuthDirective[R](directive: String => Directive1[R])(
      logic: R => Future[Either[ErrorResponse, T]]
    ): Route[String, ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint
        .in(auth.bearer[String])
        .pipe { endpoint =>
          Route(
            endpoint,
            sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
              case (token, completion) =>
                directive(token) { dependency =>
                  completion(logic(dependency))
                }
            }
          )
        }
  }
  
  implicit class RichEndpoint[I, T](endpoint: Endpoint[I, ErrorResponse, T, AkkaStreams with WebSockets]) {
    def toRoute(logic: I => Future[Either[ErrorResponse, T]]): Route[I, ErrorResponse, T, AkkaStreams with WebSockets] =
      Route(endpoint, sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(logic))
  
    def toRouteWithDirective[R](directive: Directive1[R])(
      logic: (I, R) => Future[Either[ErrorResponse, T]]
    ): Route[I, ErrorResponse, T, AkkaStreams with WebSockets] =
      Route(
        endpoint,
        directive { dependency =>
          sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
            case (input, completion) =>
              completion(logic(input, dependency))
          }
        }
      )
  
    def toRouteWithAuthDirective[R](directive: String => Directive1[R])(
      logic: (I, R) => Future[Either[ErrorResponse, T]]
    ): Route[(I, String), ErrorResponse, T, AkkaStreams with WebSockets] =
      endpoint
        .in(auth.bearer[String])
        .pipe { endpoint =>
          Route(
            endpoint,
            sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective {
              case ((input, token), completion) =>
                directive(token) { dependency =>
                  completion(logic(input, dependency))
                }
            }
          )
        }
  }
  
  implicit class RichResponse[T, TT](future: Future[Either[TT, T]]) {
    def toOut[A <: Response](
      success: (T, Int) => A,
      error: (TT, Int) => ErrorResponse =
        (_, code) => Response.Default("Error", code)
    )(implicit ec: ExecutionContext): Future[Either[ErrorResponse, A]] =
      future.map {
        case Right(succ) =>
          Right(success(succ, successCode))

        case Left(err) =>
          Left(error(err, errorCode))
      }
  }
}
