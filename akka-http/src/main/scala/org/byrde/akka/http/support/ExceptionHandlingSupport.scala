package org.byrde.akka.http.support

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.akka.http.rejections.{JsonParsingRejections, TransientServiceResponseRejections}
import org.byrde.service.response.CommonsServiceResponseDictionary.{E0200, E0405, E0500}
import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.exceptions.{ClientException, ServiceResponseException}

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler}
import akka.util.ByteString

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.parser._

import scala.annotation.tailrec

trait ExceptionHandlingSupport extends FailFastCirceSupport with CORSSupport {
  import org.byrde.akka.http.logging.HttpLogging._

  def ErrorLogger: HttpErrorLogging

  def ErrorCode: Int

  def handlers: Set[RejectionHandler]

  private lazy val LocalPrinter =
    Printer.noSpaces.copy(dropNullValues = true)

  private lazy val MediaType =
    `application/json`

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

  private lazy val cachedHandler: RejectionHandler =
    registerHandlers(default, handlers)
      .withFallback(JsonParsingRejections.handler)
      .withFallback(TransientServiceResponseRejections.handler)
      .withFallback(RejectionHandler.default)

  def rejectionHandler(req: HttpRequest): RejectionHandler =
    cachedHandler
      .mapRejectionResponse {
        case res @ HttpResponse(_status, _, ent: HttpEntity.Strict, _) =>
          val status =
            _status.intValue

          val response =
            ent
              .data
              .utf8String

          //TODO: Inefficient. We already have the response serialized to Json and ready to go, however we need to re-parse it to log the issue.
          parse(response).flatMap(_.as[TransientServiceResponse[Message]]) match {
            case Right(transientServiceResponse) =>
              ErrorLogger.error(req, ServiceResponseException(transientServiceResponse))

              val transformed =
                LocalPrinter.prettyByteBuffer(transientServiceResponse.toJson, MediaType.charset.nioCharset())

              res.copy(
                status = transientServiceResponse.status,
                entity =
                  HttpEntity(
                    MediaType,
                    ByteString(transformed))
              )

            case Left(ex) =>
              val clientException =
                ClientException(normalizeString(response), ErrorCode, status)

              val transformed =
                LocalPrinter.prettyByteBuffer(clientException.toJson, MediaType.charset.nioCharset())

              ErrorLogger.error(req, clientException)

              res.copy(
                status = status,
                entity =
                  HttpEntity(
                    MediaType,
                    ByteString(transformed))
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

        ctx.complete(serviceException.toJson)
    }

  private def registerHandlers(initialHandlder: RejectionHandler, handlersToBeRegistered: Set[RejectionHandler]): RejectionHandler = {
    @tailrec
    def innerRegisterHandlers(iterator: Iterator[RejectionHandler], innerHandler: RejectionHandler): RejectionHandler =
      if (iterator.hasNext)
        innerRegisterHandlers(iterator, innerHandler.withFallback(iterator.next))
      else
        innerHandler

    innerRegisterHandlers(handlersToBeRegistered.toIterator, initialHandlder)
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
