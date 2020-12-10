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

trait ByrdeHttpServer extends RouteSupport with CorsSupport with RejectionHandlingSupport with ExceptionHandlingSupport {
  self =>
  
  object Ack extends ByrdeResponse.Default("Success", successCode)
  
  object Err extends ByrdeResponse.Default("Error", errorCode)
  
  trait ByrdeRoutesMixin extends RouteSupport {
    override implicit def options: AkkaHttpServerOptions = self.options
    
    override def successCode: Int = self.provider.successCode
    
    def Ack: ByrdeHttpServer.this.Ack.type = self.Ack
    
    def Err: ByrdeHttpServer.this.Err.type = self.Err
  
    protected def endpointAck(
      mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] = defaultMapper
    ): Endpoint[Unit, ByrdeErrorResponse, ByrdeResponse.Default, Any] =
      self.endpointAck(mapper)
    
    protected def endpoint[T](
      description: String = "Response Body.",
      example: Option[T] = Option.empty,
      mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] = defaultMapper
    )(
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
    ): Endpoint[Unit, ByrdeErrorResponse, T, Any] =
      self.endpoint[T](
        description,
        example,
        mapper
      )
    
    def routes: ByrdeRoutes
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
  
  lazy val defaultMatcher: EndpointOutput.StatusMapping[ByrdeErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ByrdeErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Err)
    ) {
      case err: ByrdeErrorResponse if err.code == errorCode => true
    }
  
  lazy val defaultMapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] =
    sttp.tapir.oneOf[ByrdeErrorResponse](defaultMatcher)
  
  def endpointAck(
    mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] = defaultMapper
  ): Endpoint[Unit, ByrdeErrorResponse, ByrdeResponse.Default, Any] =
    sttp.tapir.endpoint.out {
      jsonBody[ByrdeResponse.Default]
        .description(s"Default response! Success code: $successCode")
        .example(Ack)
    }.errorOut(mapper)
  
  def endpoint[T](
    description: String = "Response Body.",
    example: Option[T] = Option.empty,
    mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] = defaultMapper
  )(
    implicit encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
    validator: Validator[T],
  ): Endpoint[Unit, ByrdeErrorResponse, T, Any] =
    jsonBody[T]
      .description(description)
      .pipe(out => example.fold(out)(out.example))
      .pipe(sttp.tapir.endpoint.out(_))
      .pipe(_.errorOut(mapper))

  def ping: ByrdeRoute[Unit, ByrdeErrorResponse, ByrdeResponse.Default, AkkaStreams with capabilities.WebSockets] =
    endpointAck(mapper = defaultMapper)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .toTapirRoute {
        () =>
          Future.successful {
            Right(Ack)
          }
      }
  
  def handleByrdeRoutes: Route =
    handleByrdeRoutes(Seq.empty)
  
  def handleByrdeRoutes[T <: ByrdeRoutesMixin](routes: Seq[T]): Route =
    routes
      .view
      .foldLeft(Seq.empty[ByrdeRoute[_, _, _, _]]) {
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
  
  def start[T <: ByrdeRoutesMixin](routes: Seq[T])(implicit system: ActorSystem): Unit = {
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
    (code, ByrdeResponse.Default(message, errorCode))
      .pipe(DecodeFailureHandling.response(statusCode.and(jsonBody[ByrdeErrorResponse]))(_))
}
