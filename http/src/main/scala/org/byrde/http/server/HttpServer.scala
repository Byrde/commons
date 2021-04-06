package org.byrde.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `no-cache`, `no-store`, `no-transform`, `private`}
import akka.http.scaladsl.model.headers.{RawHeader, `Cache-Control`, `Strict-Transport-Security`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives

import org.byrde.http.server.conf.{AkkaHttpConfig, CorsConfig}
import org.byrde.http.server.support.RequestIdSupport.IdHeader
import org.byrde.http.server.support._
import org.byrde.logging.Logger

import java.util.UUID

import io.circe.generic.auto._

import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.akkahttp.SwaggerAkka

import scala.concurrent.Future

trait HttpServer
  extends EndpointSupport
    with CorsSupport
    with RejectionHandlingSupport
    with ExceptionHandlingSupport {
  self =>
  
  def config: AkkaHttpConfig
  
  def logger: Logger
  
  override def corsConfig: CorsConfig = config.corsConfig
  
  private lazy val version: String =
    s"v${config.version}"
  
  private lazy val requestId: Directive1[IdHeader] =
    extractRequestContext.flatMap { ctx =>
      provide {
        ctx
          .request
          .headers
          .find(_.name().equalsIgnoreCase(IdHeader.name))
          .map(_.value())
          .map(IdHeader.apply)
          .getOrElse(IdHeader(UUID.randomUUID.toString))
      }
    }
  
  private lazy val errorMatcher: EndpointOutput.StatusMapping[ErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Response.Default("Error", errorCode))
    ) {
      case err: ErrorResponse if err.code == errorCode => true
    }
  
  lazy val errorMapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    sttp.tapir.oneOf[ErrorResponse](errorMatcher)

  def ping: MaterializedRoute[Unit, ErrorResponse, Response.Default, AkkaStreams with capabilities.WebSockets] =
    endpoint
      .out(ackOutput)
      .errorOut(errorMapper)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .route(() => Future.successful(Right(Response.Default("Success", successCode))))
  
  def handleMaterializedRoutes(routes: MaterializedRoutes): Route =
    routes
      .routes
      .view
      .pipe(_ :+ ping)
      .foldLeft[(Route, Seq[Endpoint[_, _, _, _]])]((RouteDirectives.reject, Seq.empty[Endpoint[_, _, _, _]])) {
        case ((routes, endpoints), elem) =>
          (routes ~ elem.route, endpoints :+ elem.endpoint)
      }
      .pipe {
        case (routes, endpoints) =>
          handleRoute(routes ~ handleEndpoints(endpoints))
      }
    
  def handleEndpoints(endpoints: Seq[Endpoint[_, _, _, _]]): Route =
    new SwaggerAkka(OpenAPIDocsInterpreter.toOpenAPI(endpoints, config.name, version).toYaml).routes
  
  def handleRoute(routes: Route): Route =
    (cors & requestId) { id =>
      (addRequestId(id) & addResponseId(id) & securityHeaders) {
        (handleExceptions(exceptionHandler) & handleRejections(rejectionHandler))(routes)
      }
    }
  
  def start(routes: MaterializedRoutes)(implicit system: ActorSystem): Unit = {
    val binding =
      Http()
        .newServerAt(config.interface, config.port)
        .bind(handleMaterializedRoutes(routes))
  
    system.registerOnTermination(binding.flatMap(_.unbind())(scala.concurrent.ExecutionContext.global))
    logger.info(s"${config.name} started on ${config.interface}:${config.port}")
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
        .pipe(_ :+ RawHeader("server", "api"))
        .pipe(_ :+ `Strict-Transport-Security`(16070400L, includeSubDomains = true))
        .pipe(_ :+ `Cache-Control`(`private`(), `no-cache`, `no-store`, `max-age`(0), `no-transform`))
    }
}
