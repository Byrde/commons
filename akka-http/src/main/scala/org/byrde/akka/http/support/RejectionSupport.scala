package org.byrde.akka.http.support

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.akka.http.logging.HttpLogging.ExceptionWithHttpRequestJsonLoggingFormat
import org.byrde.akka.http.rejections.RejectionException
import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.exceptions.ClientException

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import io.circe.{Encoder, Printer}
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait RejectionSupport extends FailFastCirceSupport {
  import org.byrde.akka.http.rejections.ClientExceptionRejections._

  def ErrorLogger: HttpErrorLogging

  def SuccessCode: Int

  private implicit lazy val LocalPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  def handleRejectionJson[T](
    result: T,
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    handleRejection(result, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def handleRejectionDefaultJsonResponse[T, TT <: ServiceResponse[Message]](
    result: T,
    Ok: TT,
    Err: Throwable => Throwable = identity
  ): Route =
    handleRejection(result, (_: T) => complete(Ok.toJson), Err)

  def handleRejectionCustomJsonResponse[T, TT <: ServiceResponse[Message]](
    result: T,
    Ok: T => TT,
    Err: Throwable => Throwable = identity
  ): Route =
    handleRejection(result, (res: T) => complete(Ok(res).toJson), Err)

  def handleRejection[T](
    result: T,
    Ok: T => Route,
    Err: Throwable => Throwable = identity
  ): Route =
    handle(Ok, Err)(Try(result))

  def handleRejectionAsyncJson[T](
    fn: Future[T],
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    handleRejectionAsync(fn, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def handleRejectionAsyncDefaultJsonResponse[T, TT <: ServiceResponse[Message]](
    fn: Future[T],
    Ok: TT,
    Err: Throwable => Throwable = identity
  ): Route =
    handleRejectionAsync(fn, (_: T) => complete(Ok.toJson), Err)

  def handleRejectionAsyncCustomJsonResponse[T, TT <: ServiceResponse[Message]](
    fn: Future[T],
    Ok: T => TT,
    Err: Throwable => Throwable = identity
  ): Route =
    handleRejectionAsync(fn, (res: T) => complete(Ok(res).toJson), Err)

  def handleRejectionAsync[T](
    fn: Future[T],
    Ok: T => Route,
    Err: Throwable => Throwable = identity
  ): Route =
    onComplete(fn)(handle(Ok, Err))

  private def handle[T](
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