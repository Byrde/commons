package org.byrde.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.server._

import org.byrde.http.server.conf.CorsConfig
import org.byrde.http.server.logging.HttpRequestTelemetryLog
import org.byrde.http.server.support.RequestIdSupport.IdHeader
import org.byrde.http.server.support._
import org.byrde.logging.Logger

import java.time.Instant
import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server._
import sttp.tapir.server.akkahttp._
import sttp.tapir.swagger.akkahttp.SwaggerAkka

import scala.concurrent.Future

trait HttpServer extends RouteSupport with CorsSupport with RejectionHandlingSupport with ExceptionHandlingSupport {
  self =>
  
  object Ack extends Response.Default("Success", successCode)
  
  object Err extends Response.Default("Error", errorCode)
  
  trait RoutesMixin extends RouteSupport {
    override implicit def options: AkkaHttpServerOptions = self.options
    
    override def successCode: Int = self.provider.successCode
    
    def Ack: HttpServer.this.Ack.type = self.Ack
    
    def Err: HttpServer.this.Err.type = self.Err
  
    protected def endpointAck(
      mapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] = defaultMapper
    ): Endpoint[Unit, ErrorResponse, Response.Default, Any] =
      self.endpointAck(mapper)
    
    protected def endpoint[T](
      description: String = "Response Body.",
      example: Option[T] = Option.empty,
      mapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] = defaultMapper
    )(
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
    ): Endpoint[Unit, ErrorResponse, T, Any] =
      self.endpoint[T](
        description,
        example,
        mapper
      )
    
    def routes: Routes
  }
  
  def provider: Provider
  
  def successCode: Int = self.provider.successCode
  
  def logger: Logger = self.provider.logger
  
  def corsConfig: CorsConfig = self.provider.config.corsConfig
  
  implicit lazy val options: AkkaHttpServerOptions =
    AkkaHttpServerOptions.default.copy(decodeFailureHandler = decodeFailureHandler)
  
  private lazy val version: String =
    s"v${provider.config.version}"
  
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
  
  lazy val defaultMatcher: EndpointOutput.StatusMapping[ErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Err)
    ) {
      case err: ErrorResponse if err.code == errorCode => true
    }
  
  lazy val defaultMapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    sttp.tapir.oneOf[ErrorResponse](defaultMatcher)
  
  def endpointAck(
    mapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] = defaultMapper
  ): Endpoint[Unit, ErrorResponse, Response.Default, Any] =
    sttp.tapir.endpoint.out {
      jsonBody[Response.Default]
        .description(s"Default response! Success code: $successCode")
        .example(Ack)
    }.errorOut(mapper)
  
  def endpoint[T](
    description: String = "Response Body.",
    example: Option[T] = Option.empty,
    mapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] = defaultMapper
  )(
    implicit encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
    validator: Validator[T],
  ): Endpoint[Unit, ErrorResponse, T, Any] =
    jsonBody[T]
      .description(description)
      .pipe(out => example.fold(out)(out.example))
      .pipe(sttp.tapir.endpoint.out(_))
      .pipe(_.errorOut(mapper))

  def ping: org.byrde.http.server.Route[Unit, ErrorResponse, Response.Default, AkkaStreams with capabilities.WebSockets] =
    endpointAck(mapper = defaultMapper)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .toRoute {
        () =>
          Future.successful {
            Right(Ack)
          }
      }
  
  def handleByrdeRoutes: Route =
    handleByrdeRoutes(Seq.empty)
  
  def handleByrdeRoutes[T <: RoutesMixin](routes: Seq[T]): Route =
    routes
      .view
      .foldLeft(Seq.empty[org.byrde.http.server.Route[_, _, _, _]]) {
        case (acc, elem) =>
          acc ++ elem.routes.value
      }
      .pipe(_ :+ ping)
      .foldLeft[(Route, Seq[Endpoint[_, _, _, _]])]((RouteDirectives.reject, Seq.empty[Endpoint[_, _, _, _]])) {
        case ((routes, endpoints), elem) =>
          (routes ~ elem.route, endpoints :+ elem.endpoint)
      }
      .pipe {
        case (routes, endpoints) =>
          handleRoutes(routes ~ handleEndpoints(endpoints))
      }
    
  def handleEndpoints(endpoints: Seq[Endpoint[_, _, _, _]]): Route =
    new SwaggerAkka(endpoints.toOpenAPI(provider.config.name, version).toYaml).routes
  
  def handleRoutes(routes: Route): Route =
    (cors & requestId) { id =>
      (addRequestId(id) & addResponseId(id) & extractRequest) { request =>
        Instant.now.toEpochMilli.pipe { start =>
          bagAndTag(start, request) {
            (handleExceptions(exceptionHandler) & handleRejections(rejectionHandler))(routes)
          }
        }
      }
    }
  
  def start[T <: RoutesMixin](routes: Seq[T])(implicit system: ActorSystem): Unit = {
    Http()
      .newServerAt(provider.config.interface, provider.config.port)
      .bind(handleByrdeRoutes(routes))
    
    provider.logger.info(s"${provider.config.name} started on ${provider.config.interface}:${provider.config.port}")
  }
  
  private def addRequestId(id: IdHeader): Directive0 =
    mapRequest { request =>
      request.withHeaders(id +: request.headers)
    }
  
  private def addResponseId(id: IdHeader): Directive0 =
    mapResponseHeaders { headers =>
      id +: headers
    }
  
  private def bagAndTag(start: Long, request: HttpRequest): Directive0 =
    mapResponse { response =>
      logger.info(
        "RequestResponseHandlingSupport.bagAndTag",
        HttpRequestTelemetryLog(request, response.status.intValue, System.currentTimeMillis() - start)
      )
      response
    }
  
  private def decodeFailureHandler: DefaultDecodeFailureHandler =
    ServerDefaults.decodeFailureHandler.copy(response = handleDecodeFailure)
  
  private def handleDecodeFailure(code: StatusCode, message: String): DecodeFailureHandling =
    (code, Response.Default(message, errorCode))
      .pipe(DecodeFailureHandling.response(statusCode.and(jsonBody[ErrorResponse]))(_))
}
