package org.byrde.http.server

import org.byrde.http.server.conf.ServerConfig
import org.byrde.http.server.support.{CorsSupport, ExceptionHandlingSupport, MaterializedEndpointSupport, RejectionHandlingSupport, RequestIdSupport}
import org.byrde.http.server.support.RequestIdSupport.IdHeader
import org.byrde.logging.Logger

import io.circe.generic.auto._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.CacheDirectives._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives

import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future
import scala.util.ChainingSyntax

trait Server
  extends MaterializedEndpointSupport
    with CorsSupport
    with RequestIdSupport
    with ExceptionHandlingSupport
    with RejectionHandlingSupport
    with ChainingSyntax {
  private lazy val ackOutput: EndpointIO.Body[String, Response.Default] =
    jsonBody[Response.Default]
      .description(s"Default response!")
      .example(Response.Default("Success"))
  
  private def ping =
    endpoint
      .out(ackOutput)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .errorOut(jsonBody[ErrorResponse.Default])
      .toMaterializedEndpoint(Future.successful(Right(Response.Default("Success"))))
  
  private def handleMaterializedEndpoints(endpoints: AnyMaterializedEndpoints)(implicit config: ServerConfig, logger: Logger): Route =
    endpoints
      .view
      .pipe(_ :+ ping)
      .foldLeft[(Route, Seq[AnyEndpoint])]((RouteDirectives.reject, Seq.empty[AnyEndpoint])) {
        case ((routes, endpoints), elem) =>
          (routes ~ elem.route, endpoints :+ elem.endpoint)
      }
      .pipe {
        case (routes, endpoints) =>
          handleRoute(routes ~ handleEndpoints(endpoints)(config))(config, logger)
      }
  
  private def handleEndpoints(endpoints: Seq[AnyEndpoint])(implicit config: ServerConfig): Route =
    AkkaHttpServerInterpreter().toRoute {
      SwaggerUI[Future](OpenAPIDocsInterpreter().toOpenAPI(endpoints, config.name, config.version).toYaml)
    }
  
  private def handleRoute(routes: Route)(implicit config: ServerConfig, logger: Logger): Route =
    (corsDirective(config.corsConfig) & requestId) { id =>
      (addRequestId(id) & addResponseId(id) & securityHeaders) {
        (handleExceptions(exceptionHandler(logger)) & handleRejections(rejectionHandler))(routes)
      }
    }
  
  private def addRequestId(id: IdHeader): Directive0 =
    mapRequest { request =>
      request.withHeaders(id +: request.headers)
    }
  
  private def addResponseId(id: IdHeader): Directive0 =
    mapResponseHeaders { headers =>
      id +: headers
    }
  
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
    endpoints: AnyMaterializedEndpoints = Seq.empty
  )(implicit config: ServerConfig, logger: Logger, system: ActorSystem): Unit = {
    Http()
      .newServerAt(config.interface, config.port)
      .bind(handleMaterializedEndpoints(endpoints))
      .tap(binding => system.registerOnTermination(binding.flatMap(_.unbind())(scala.concurrent.ExecutionContext.global)))
    
    logger.logInfo(s"${config.name} started on ${config.interface}:${config.port}")
  }
}
