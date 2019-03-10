package org.byrde.akka.http.support

import org.byrde.akka.http.rejections.RejectionException
import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.ServiceResponse

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Encoder, Printer}
import io.circe.generic.semiauto._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RouteSupport extends FailFastCirceSupport {
  def SuccessCode: Int

  private implicit lazy val LocalPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  def asyncJson[T](
    fn: Future[T],
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    async(fn, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def asyncWithDefaultJsonResponse[T, TT <: ServiceResponse[Message]](
    fn: Future[T],
    Ok: TT,
    Err: Throwable => Throwable = identity
  ): Route =
    async(fn, (_: T) => complete(Ok.toJson(deriveEncoder[Message])), Err)

  def asyncWithCustomJsonResponse[T, TT <: ServiceResponse[Message]](
    fn: Future[T],
    Ok: T => TT,
    Err: Throwable => Throwable = identity
  ): Route =
    async(fn, (res: T) => complete(Ok(res).toJson(deriveEncoder[Message])), Err)

  def async[T](
    fn: Future[T],
    Ok: T => Route,
    Err: Throwable => Throwable = identity
  ): Route =
    onComplete(fn) {
      case Success(res) =>
        Ok(res)

      case Failure(ex) =>
        ex match {
          case ex: RejectionException =>
            reject(ex)

          case _ =>
            throw Err(ex)
        }
    }
}
