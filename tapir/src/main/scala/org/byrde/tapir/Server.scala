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
  
  trait TapirRoutes extends RouteSupport {
    override def SuccessCode: Int = self.provider.SuccessCode
  
    override def ErrorCode: Int = self.provider.ErrorCode
    
    protected def endpoint[T <: TapirResponse](
      implicit encoder: Encoder[T],
      decoder: Decoder[T],
      schema: Schema[T],
      validator: Validator[T],
    ): Endpoint[Unit, TapirErrorResponse, T, Any] =
      self.endpoint[T]
    
    def routes: Seq[TapirRoute]
  }
  
  def provider: Provider
  
  def mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse]
  
  def SuccessCode: Int = self.provider.SuccessCode
  
  def ErrorCode: Int = self.provider.ErrorCode
  
  def logger: Logger = self.provider.logger
  
  def cors: CorsConfig = self.provider.config.cors
  
  implicit val options: AkkaHttpServerOptions =
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
   
  def ping: Endpoint[Unit, TapirErrorResponse, TapirResponse.Default, Any] =
    endpoint[TapirResponse.Default]
      .name("Ping")
      .summary("Say hello!")
      .description("Standard API endpoint to say hello to the server.")
      .in("ping")
  
  def handleTapirRoutes(routes: Seq[TapirRoutes]): Route =
    routes
      .view
      .foldLeft(Seq.empty[TapirRoute]) {
        case (acc, elem) =>
          acc ++ elem.routes
      }
      .foldLeft[(Route, Seq[Endpoint[_, _, _, _]])]((RouteDirectives.reject, Seq.empty[Endpoint[_, _, _, _]])) {
        case ((routes, endpoints), elem) =>
          (routes ~ elem.route, endpoints :+ elem.endpoint)
      }
      .pipe {
        case (routes, endpoints) =>
          handleRoutes(routes ~ handleEndpoints(endpoints))
      }
    
  def handleEndpoints(endpoints: Seq[Endpoint[_, _, _, _]]): Route =
    new SwaggerAkka((endpoints :+ ping)
      .toOpenAPI(provider.config.name, version).toYaml).routes
  
  def handleRoutes(route: Route): Route =
    cors {
      requestId { id =>
        addRequestId(id) {
          addResponseId(id) {
            extractRequest { request =>
              Instant.now.toEpochMilli.pipe { start =>
                bagAndTag(start, request) {
                  handleExceptions(exceptionHandler) {
                    route ~ ping.toRoute { _ =>
                      Future.successful {
                        Right(TapirResponse.Default(SuccessCode))
                      }
                    }
                  }
                }
              }
            }
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
    (code, TapirResponse.Default(provider.ErrorCode))
      .pipe(DecodeFailureHandling.response(statusCode.and(jsonBody[TapirErrorResponse]))(_))
}