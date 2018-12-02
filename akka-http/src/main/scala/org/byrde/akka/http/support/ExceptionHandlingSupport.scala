package org.byrde.akka.http.support

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.akka.http.rejections.{JsonParsingRejections, TransientServiceResponseRejections}
import org.byrde.service.response.CommonsServiceResponseDictionary.{E0200, E0405, E0500}
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.exceptions.{ClientException, ServiceResponseException}
import org.byrde.utils.JsonUtils

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler}

import play.api.libs.json.{JsNull, JsSuccess, Json}

import scala.annotation.tailrec
import scala.util.Try

trait ExceptionHandlingSupport extends PlayJsonSupport with CORSSupport {
  import org.byrde.akka.http.logging.HttpLogging._

  def ErrorLogger: HttpErrorLogging

  def ErrorCode: Int

  def handlers: Set[RejectionHandler]

  private lazy val default: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[MethodRejection] { rejections =>
        val methods =
          rejections.map(_.supported)

        val names =
          methods
            .map(_.name)
            .mkString(", ")

        respondWithHeader(Allow(methods)) {
          options {
            complete(E0200(s"Supported methods: $names").toJson)
          }
        } ~ complete(E0405(s"HTTP method not allowed, supported methods: $names", ErrorCode).toJson)
      }
      .result()

  implicit lazy val rejectionHandler: RejectionHandler =
    registerHandlers
      .withFallback(JsonParsingRejections.handler)
    .withFallback(TransientServiceResponseRejections.handler)
      .withFallback(RejectionHandler.default)
      .mapRejectionResponse {
        case res @ HttpResponse(_status, _, ent: HttpEntity.Strict, _) =>
          val status =
            _status.intValue

          val response =
            ent
              .data
              .utf8String

          //TODO: Inefficient. We already have the response serialized to Json and ready to go, however we need to re-parse it to log the issue.
          Try(Json.parse(response))
            .toOption
            .getOrElse(JsNull)
            .validate[TransientServiceResponse[String]](ServiceResponse.reads(JsonUtils.Format.string(ServiceResponse.message))) match {
              case JsSuccess(transientServiceResponse, _) =>
                res.copy(
                  status = transientServiceResponse.status,
                  entity =
                    HttpEntity(
                      ContentTypes.`application/json`,
                      Json.stringify(transientServiceResponse.toJson)
                    )
                )

              case _ =>
                val clientException =
                  ClientException(normalizeString(response), ErrorCode, status)

                res.copy(
                  status = status,
                  entity =
                    HttpEntity(
                      ContentTypes.`application/json`,
                      Json.stringify(clientException.toJson)
                    )
                )
            }

        case res =>
          res
      }

  //Use ExceptionHandler for all server errors
  lazy val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case exception: Throwable => ctx =>
        val serviceException =
          exception match {
            case serviceException: ServiceResponseException[_] =>
              ErrorLogger.error(ctx.request, serviceException)
              serviceException
            case _ =>
              ErrorLogger.error(ctx.request, exception)
              E0500(exception)
          }

        ctx.complete(Json.toJson(serviceException))
    }

  private def registerHandlers: RejectionHandler = {
    @tailrec
    def innerRegisterHandlers(iterator: Iterator[RejectionHandler], innerHandler: RejectionHandler): RejectionHandler =
      if (iterator.hasNext)
        innerRegisterHandlers(iterator, innerHandler.withFallback(iterator.next))
      else
        innerHandler

    innerRegisterHandlers(handlers.toIterator, default)
  }

  private def stripLeadingAndTrailingQuotes(value: String): String = {
    var tmp =
      value

    if (tmp.startsWith("\""))
      tmp = tmp.substring(1, tmp.length)

    if (value.endsWith("\""))
      tmp = tmp.substring(0, tmp.length - 1)

    tmp
  }

  private def removeNewLine(value: String): String =
    value.replaceAll("\n", "")

  private def normalizeString(value: String): String =
    removeNewLine(stripLeadingAndTrailingQuotes(value))
}
