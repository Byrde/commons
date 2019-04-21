package org.byrde.akka.http.support

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.akka.http.logging.HttpLogging.ExceptionWithHttpRequestJsonLoggingFormat
import org.byrde.akka.http.rejections.RejectionException
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.exceptions.ClientException

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import io.circe.{Encoder, Printer}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ResponseSupport extends FailFastCirceSupport {
  import org.byrde.akka.http.rejections.ClientExceptionRejections._

  def ErrorLogger: HttpErrorLogging

  def SuccessCode: Int

  private implicit lazy val LocalPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  def json[T](
    result: T,
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    handle(result, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def handle[T](
    result: T,
    Ok: T => Route,
    Err: Throwable => Throwable = identity
  ): Route =
    innerHandle(Ok, Err)(Try(result))

  def asyncJson[T](
    fn: Future[T],
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    handleAsync(fn, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def handleAsync[T](
    fn: Future[T],
    Ok: T => Route,
    Err: Throwable => Throwable = identity
  ): Route =
    onComplete(fn)(innerHandle(Ok, Err))

  private def innerHandle[T](
    Ok: T => Route,
    Err: Throwable => Throwable
  ): PartialFunction[Try[T], Route] = {
    case Success(res) =>
      Ok(res)

    case Failure(ex) =>
      ex match {
        case ex: ClientException =>
          extractRequest { req =>
            ErrorLogger.error(req, ex)
            reject(ex.toRejection)
          }

        case ex: RejectionException =>
          extractRequest { req =>
            ErrorLogger.error(req, ex)
            reject(ex)
          }

        case _ =>
          throw Err(ex)
      }
  }
}