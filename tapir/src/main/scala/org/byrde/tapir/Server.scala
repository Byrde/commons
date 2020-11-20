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

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

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
    override def successCode: Int = self.provider.successCode
    
    protected def endpoint[T <: TapirResponse](
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
    ): Endpoint[Unit, TapirErrorResponse, T, Any] =
      self.endpoint[T]
    
    def routes: TapirRoutes
  }
  
  def provider: Provider
  
  def mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse]
  
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
  
  def endpoint[T <: TapirResponse](
    implicit encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
    validator: Validator[T],
  ): Endpoint[Unit, TapirErrorResponse, T, Any] =
    sttp.tapir.endpoint.out(jsonBody[T]).errorOut(mapper)
   
  def ping: TapirRoute =
    endpoint[TapirResponse.Default]
      .name("Ping")
      .summary("Say hello!")
      .description("Standard API endpoint to say hello to the server.")
      .get
      .in("ping")
      .toTapirRoute { _ =>
        Future.successful {
          Right(TapirResponse.Default(successCode))
        }
      }
  
  def handleTapirRoutes[T <: TapirRoutesMixin](routes: Seq[T]): Route =
    routes
      .view
      .foldLeft(Seq.empty[TapirRoute]) {
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
