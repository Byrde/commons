package org.byrde.http.server

import org.byrde.http.server.conf.ServerConfig
import org.byrde.http.server.support._

import io.circe.generic.auto._

import sttp.apispec.openapi.circe.yaml._
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.chaining._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.CacheDirectives._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives

trait Server extends MaterializedEndpointSupport with CorsSupport {
  private lazy val ackOutput: EndpointIO.Body[String, Ack] =
    jsonBody[Ack].description(s"Default response!").example(Ack("Success"))

  private def ping =
    endpoint
      .out(ackOutput)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .toMaterializedEndpoint()(Future.successful(Right(Ack("Success"))))(scala.concurrent.ExecutionContext.global)

  private def handleMaterializedEndpoints(
    endpoints: AnyMaterializedEndpoints,
  )(implicit ec: ExecutionContext, config: ServerConfig): Route =
    endpoints
      .view
      .pipe(_ :+ ping)
      .foldLeft[(Route, Seq[AnyEndpoint])]((RouteDirectives.reject, Seq.empty[AnyEndpoint])) {
        case ((routes, endpoints), elem) =>
          (routes ~ elem.route, if (elem.hide) endpoints else endpoints :+ elem.endpoint)
      }
      .pipe {
        case (routes, endpoints) =>
          handleRoute(routes ~ handleEndpoints(endpoints)(ec, config))(config)
      }

  private def handleEndpoints(endpoints: Seq[AnyEndpoint])(implicit ec: ExecutionContext, config: ServerConfig): Route =
    AkkaHttpServerInterpreter().toRoute {
      SwaggerUI[Future](OpenAPIDocsInterpreter().toOpenAPI(endpoints, config.name, config.version).toYaml)
    }

  private def handleRoute(routes: Route)(implicit config: ServerConfig): Route =
    (corsDirective(config.corsConfig) & securityHeaders)(routes)

  private def securityHeaders: Directive0 =
    mapResponseHeaders { response =>
      response
        .pipe(_.filterNot(_.lowercaseName() == "server"))
        .pipe(_.filterNot(_.lowercaseName() == "strict-transport-security"))
        .pipe(_.filterNot(_.lowercaseName() == "cache-control"))
        //        .pipe(_ :+ RawHeader("server", "api"))
        .pipe(_ :+ `Strict-Transport-Security`(16070400L, includeSubDomains = true))
        .pipe(_ :+ `Cache-Control`(`private`(), `no-cache`, `no-store`, `max-age`(0), `no-transform`))
    }

  def start(
    config: ServerConfig,
    endpoints: AnyMaterializedEndpoints = Seq.empty,
  )(implicit system: ActorSystem, ec: ExecutionContext): Unit =
    Http()
      .newServerAt(config.interface, config.port)
      .bind(handleMaterializedEndpoints(endpoints)(ec, config))
      .tap(binding => system.registerOnTermination(binding.flatMap(_.unbind())(scala.concurrent.ExecutionContext.global)))
}
