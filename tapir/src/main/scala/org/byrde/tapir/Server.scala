package org.byrde.tapir

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives

import org.byrde.logging.Logger
import org.byrde.tapir.conf.CorsConfig
import org.byrde.tapir.logging.HttpRequestTelemetryLog
import org.byrde.tapir.support.RequestIdSupport.IdHeader
import org.byrde.tapir.support._

import java.time.Instant
import java.util.UUID

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}

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

trait Server extends RouteSupport with CorsSupport with ExceptionSupport {
  self =>
  
  trait TapirRoutesMixin extends RouteSupport {
    override implicit def options: AkkaHttpServerOptions = self.options
    
    override def successCode: Int = self.provider.successCode
  
    protected def endpointAck(
      mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] = defaultMapper
    ): Endpoint[Unit, TapirErrorResponse, TapirResponse.Default, Any] =
      self.endpointAck(mapper)
    
    protected def endpoint[T <: TapirResponse](
      description: String = "Response Body.",
      example: Option[T] = Option.empty,
      mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] = defaultMapper
    )(
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
    ): Endpoint[Unit, TapirErrorResponse, T, Any] =
      self.endpoint[T](description, example, mapper)
    
    def routes: TapirRoutes
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
  
  lazy val defaultMatcher: EndpointOutput.StatusMapping[TapirErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[TapirErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(TapirResponse.Default(errorCode))
    ) {
      case err: TapirErrorResponse if err.code == errorCode => true
    }
  
  lazy val defaultMapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] =
    sttp.tapir.oneOf[TapirErrorResponse](defaultMatcher)
  
  def endpointAck(
    mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] = defaultMapper
  ): Endpoint[Unit, TapirErrorResponse, TapirResponse.Default, Any] =
    sttp.tapir.endpoint.out {
      jsonBody[TapirResponse.Default]
        .description(s"Default response! Success code: $successCode")
        .example(TapirResponse.Default(successCode))
    }.errorOut(mapper)
  
  def endpoint[T](
    description: String = "",
    example: Option[T] = Option.empty,
    mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] = defaultMapper
  )(
    implicit encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
    validator: Validator[T],
  ): Endpoint[Unit, TapirErrorResponse, T, Any] =
    jsonBody[T]
      .description(description)
      .pipe(out => example.fold(out)(out.example))
      .pipe(sttp.tapir.endpoint.out(_))
      .pipe(_.errorOut(mapper))

  def ping: TapirRoute[Unit, TapirErrorResponse, TapirResponse.Default, AkkaStreams with capabilities.WebSockets] =
    endpointAck(mapper = defaultMapper)
      .get
      .in("ping")
      .name("Ping")
      .description("Standard API endpoint to say hello to the server.")
      .toTapirRoute { _ =>
        Future.successful {
          Right(TapirResponse.Default(successCode))
        }
      }
  
  def handleTapirRoutes: Route =
    handleTapirRoutes(Seq.empty)
  
  def handleTapirRoutes[T <: TapirRoutesMixin](routes: Seq[T]): Route =
    routes
      .view
      .foldLeft(Seq.empty[TapirRoute[_, _, _, _]]) {
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
            handleExceptions(exceptionHandler)(routes)
          }
        }
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
  
  private def handleDecodeFailure(code: StatusCode, _message: String): DecodeFailureHandling =
    (code, TapirResponse.Default(errorCode))
      .pipe(DecodeFailureHandling.response(statusCode.and(jsonBody[TapirErrorResponse]))(_))
}
