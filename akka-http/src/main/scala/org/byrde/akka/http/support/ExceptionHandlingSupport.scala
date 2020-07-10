package org.byrde.akka.http.support

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler}
import akka.util.ByteString

import org.byrde.akka.http.logging.HttpRequestLog
import org.byrde.akka.http.rejections.{ClientExceptionRejections, JsonParsingRejections}
import org.byrde.akka.http.support.CirceSupport.FailFastCirceSupport
import org.byrde.logging.{Logger, Logging}
import org.byrde.service.response.CommonsServiceResponseDictionary.{E0405, E0500}
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.exceptions.{ClientException, ServiceResponseException}
import org.byrde.service.response.{Message, Status}

import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{Json, Printer}

import scala.annotation.tailrec

trait ExceptionHandlingSupport extends FailFastCirceSupport with CORSSupport with Logging {

  def Ack: Json
  
  def logger: Logger

  def ErrorCode: Int

  def handlers: Set[RejectionHandler]

  implicit def printer: Printer

  private lazy val MediaType =
    `application/json`

  private lazy val default: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[MethodRejection] { rejections =>
        val methods =
          rejections.map(_.supported)

        respondWithHeader(Allow(methods)) {
          options {
            complete(Ack)
          }
        } ~ complete(E0405(ErrorCode).toJson)
      }
      .result()

  private lazy val cachedHandler: RejectionHandler =
    registerHandlers(default, handlers)
      .withFallback(ClientExceptionRejections.handler)
      .withFallback(JsonParsingRejections.handler)
      .withFallback(RejectionHandler.default)

  lazy val rejectionHandler: RejectionHandler =
    cachedHandler
      .mapRejectionResponse {
        case res @ HttpResponse(_status, _, ent: HttpEntity.Strict, _) =>
          val status =
            _status.intValue

          val response =
            ent
              .data
              .utf8String

          parse(response)
            .flatMap(_.as[TransientServiceResponse[Option[Message]]])
            .map(_.status)
            .map { status =>
              res.copy(status = status.value)
            }
            .getOrElse {
              def clientException =
                ClientException(normalizeString(response), Status.fromInt(status), ErrorCode)

              def transformed =
                printer.printToByteBuffer(clientException.toJson, MediaType.charset.nioCharset())

              res.copy(
                status = status,
                entity =
                  HttpEntity(
                    MediaType,
                    ByteString(transformed)
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
              error(HttpRequestLog(ctx.request), serviceException)
              serviceException

            case _ =>
              error(HttpRequestLog(ctx.request), exception)
              E0500(exception)(ErrorCode)
          }

        ctx.complete(serviceException.toJson)
    }

  private def registerHandlers(initialHandler: RejectionHandler, handlersToBeRegistered: Set[RejectionHandler]): RejectionHandler = {
    @tailrec
    def innerRegisterHandlers(iterator: Iterator[RejectionHandler], innerHandler: RejectionHandler): RejectionHandler =
      if (iterator.hasNext)
        innerRegisterHandlers(iterator, innerHandler.withFallback(iterator.next))
      else
        innerHandler

    innerRegisterHandlers(handlersToBeRegistered.iterator, initialHandler)
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
