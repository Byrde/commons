package org.byrde.akka.http.support

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import org.byrde.akka.http.logging.HttpRequestLog
import org.byrde.akka.http.rejections.RejectionException
import org.byrde.akka.http.support.CirceSupport.FailFastCirceSupport
import org.byrde.logging.{AkkaLogger, Logging}
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.exceptions.ClientException

import io.circe.{Encoder, Json, Printer}
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ResponseSupport extends FailFastCirceSupport with Logging {

  import org.byrde.akka.http.rejections.ClientExceptionRejections._

  def Ack: Json

  def ErrorLogger: AkkaLogger

  def SuccessCode: Int

  def HandleThrowable: Throwable => Route =
    throw _

  implicit def printer: Printer

  def handleJson[T](
    result: T,
    code: Int = SuccessCode,
  )(implicit encoder: Encoder[T]): Route =
    handle(result, (res: T) => complete(ServiceResponse(code, res).toJson))

  def handleAck[T](result: T): Route =
    handle(result, (_: T) => complete(Ack))

  def handle[T](
    result: T,
    Ok: T => Route,
  ): Route =
    innerHandle(Ok)(Try(result))

  def handleAsyncJson[T](
    fn: Future[T],
    code: Int = SuccessCode,
  )(implicit encoder: Encoder[T]): Route =
    handleAsync(fn, (res: T) => complete(ServiceResponse(code, res).toJson))

  def handleAsyncAck[T](fn: Future[T]): Route =
    handleAsync(fn, (_: T) => complete(Ack))

  def handleAsync[T](
    fn: Future[T],
    Ok: T => Route,
  ): Route =
    onComplete(fn)(innerHandle(Ok))

  protected def innerHandle[T](
    Ok: T => Route,
  ): PartialFunction[Try[T], Route] = {
    case Success(res) =>
      Ok(res)

    case Failure(ex) =>
      ex match {
        case ex: ClientException =>
          extractRequest { req =>
            error(HttpRequestLog(req), ex).provide(ErrorLogger)
            reject(ex.toRejection)
          }

        case ex: RejectionException =>
          extractRequest { req =>
            error(HttpRequestLog(req), ex).provide(ErrorLogger)
            reject(ex)
          }

        case _ =>
          HandleThrowable(ex)
      }
  }

}
