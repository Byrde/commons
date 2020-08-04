package org.byrde.akka.http.support

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import org.byrde.akka.http.logging.HttpRequestLog
import org.byrde.akka.http.rejections.RejectionException
import org.byrde.akka.http.support.CirceSupport.FailFastCirceSupport
import org.byrde.logging.Logger
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.exceptions.ClientException

import io.circe.{Encoder, Json, Printer}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ResponseSupport extends FailFastCirceSupport {

  import org.byrde.akka.http.rejections.ClientExceptionRejections._
  
  def logger: Logger

  def Ack: Json

  def SuccessCode: Int

  def handle: Throwable => Route

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
            logger.error("ResponseSupport.innerHandle: ClientException (1)", ex)
            logger.error("ResponseSupport.innerHandle: ClientException (2)", HttpRequestLog(req))
            reject(ex.toRejection)
          }

        case ex: RejectionException =>
          extractRequest { req =>
            logger.error("ResponseSupport.innerHandle: RejectionException (1)", ex)
            logger.error("ResponseSupport.innerHandle: RejectionException (2)", HttpRequestLog(req))
            reject(ex)
          }

        case _ =>
          handle(ex)
      }
  }

}
