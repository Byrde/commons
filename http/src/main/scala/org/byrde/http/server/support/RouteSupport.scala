package org.byrde.http.server.support

import akka.http.scaladsl.server._

import org.byrde.http.server.{ByrdeResponse, ByrdeRoute, ByrdeRoutes, _}

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.server.akkahttp._

import scala.concurrent.{ExecutionContext, Future}

trait RouteSupport extends RequestSupport with ResponseSupport with WithSuccessAndErrorCode with RequestIdSupport {
  implicit def options: AkkaHttpServerOptions
  
  implicit def tapirRoute2TapirRoutes(route: ByrdeRoute[_, _, _, _]): ByrdeRoutes =
    ByrdeRoutes(route)
  
  implicit class ChainTapirRoute(route: ByrdeRoute[_, _, _, _]) {
    def ~ (_route: ByrdeRoute[_, _, _, _]): ByrdeRoutes =
      ByrdeRoutes(Seq(route, _route))
  }
  
  implicit class ChainTapirRoutes(routes: ByrdeRoutes) {
    def ~ (route: ByrdeRoute[_, _, _, _]): ByrdeRoutes =
      ByrdeRoutes(routes.value :+ route)
  }
  
  implicit class RichUnitEndpoint[T](endpoint: Endpoint[Unit, ByrdeErrorResponse, T, AkkaStreams with WebSockets]) {
    def toRoute(logic: () => Future[Either[ByrdeErrorResponse, T]]): Route =
      sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(_ => logic())
    
    def toDirective: Directive[(Unit, Future[Either[ByrdeErrorResponse, T]] => Route)] =
      sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective
    
    def toTapirRoute(logic: () => Future[Either[ByrdeErrorResponse, T]]): ByrdeRoute[Unit, ByrdeErrorResponse, T, AkkaStreams with WebSockets] =
      ByrdeRoute(endpoint, endpoint.toRoute(logic))
    
    def toTapirRouteWithDirective[R](
      logic: R => Future[Either[ByrdeErrorResponse, T]]
    )(directive: Directive1[R]): ByrdeRoute[Unit, ByrdeErrorResponse, T, AkkaStreams with WebSockets] =
      ByrdeRoute(
        endpoint,
        directive { value =>
          endpoint.toDirective {
            case (_, completion) =>
              completion(logic(value))
          }
        }
      )
  }
  
  implicit class RichEndpoint[I, T](endpoint: Endpoint[I, ByrdeErrorResponse, T, AkkaStreams with WebSockets]) {
    def toRoute(logic: I => Future[Either[ByrdeErrorResponse, T]]): Route =
      sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toRoute(logic)
  
    def toDirective: Directive[(I, Future[Either[ByrdeErrorResponse, T]] => Route)] =
      sttp.tapir.server.akkahttp.RichAkkaHttpEndpoint(endpoint).toDirective
    
    def toTapirRoute(logic: I => Future[Either[ByrdeErrorResponse, T]]): ByrdeRoute[I, ByrdeErrorResponse, T, AkkaStreams with WebSockets] =
      ByrdeRoute(endpoint, endpoint.toRoute(logic))
  
    def toTapirRouteWithDirective[R](
      logic: (I, R) => Future[Either[ByrdeErrorResponse, T]]
    )(directive: Directive1[R]): ByrdeRoute[I, ByrdeErrorResponse, T, AkkaStreams with WebSockets] =
      ByrdeRoute(
        endpoint,
        directive { dependency =>
          endpoint.toDirective {
            case (input, completion) =>
              completion(logic(input, dependency))
          }
        }
      )
  }
  
  implicit class RichResponse[T, TT](future: Future[Either[TT, T]]) {
    def toOut[A <: ByrdeResponse](
      success: (T, Int) => A,
      error: (TT, Int) => ByrdeErrorResponse =
        (_, code) => ByrdeResponse.Default("Error", code)
    )(implicit ec: ExecutionContext): Future[Either[ByrdeErrorResponse, A]] =
      future.map {
        case Right(succ) =>
          Right(success(succ, successCode))

        case Left(err) =>
          Left(error(err, errorCode))
      }
  }
}
