package org.byrde.akka.http.support

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.akka.http.rejections.{ClientExceptionRejections, JsonParsingRejections, TransientServiceResponseRejections}
import org.byrde.service.response.CommonsServiceResponseDictionary.{E0200, E0405, E0500}
import org.byrde.service.response.exceptions.ServiceResponseException

import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import io.circe.Printer
import io.circe.generic.auto._

import scala.annotation.tailrec

trait ExceptionHandlingSupport extends FailFastCirceSupport with CORSSupport {
  import org.byrde.akka.http.logging.HttpLogging._

  def ErrorLogger: HttpErrorLogging

  def ErrorCode: Int

  def handlers: Set[RejectionHandler]

  private implicit lazy val LocalPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

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

  lazy val rejectionHandler: RejectionHandler =
    registerHandlers(default, handlers)
      .withFallback(JsonParsingRejections.handler)
      .withFallback(ClientExceptionRejections.handler)
      .withFallback(TransientServiceResponseRejections.handler)
      .withFallback(RejectionHandler.default)

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
}
