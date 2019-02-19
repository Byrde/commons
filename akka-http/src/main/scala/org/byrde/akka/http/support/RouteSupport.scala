package org.byrde.akka.http.support

import org.byrde.akka.http.rejections.RejectionException
import org.byrde.service.response.ServiceResponse

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route

import io.circe.Encoder

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RouteSupport extends FailFastCirceSupport {
  def SuccessCode: Int

  def asyncJson[T](
    fn: Future[T],
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    async(fn, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def asyncServiceResponse[T, TT <: ServiceResponse[T]](
    fn: Future[TT],
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[T]): Route =
    async(fn, (res: TT) => complete(res.toJson), Err)

  def asyncWithDefaultJsonResponse[T, TT, TTT <: ServiceResponse[TT]](
    fn: Future[T],
    Ok: TTT,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[TT]): Route =
    async(fn, (_: T) => complete(Ok.toJson), Err)

  def asyncWithCustomJsonResponse[T, TT, TTT <: ServiceResponse[TT]](
    fn: Future[T],
    Ok: T => TTT,
    Err: Throwable => Throwable = identity
  )(implicit encoder: Encoder[TT]): Route =
    async(fn, (res: T) => complete(Ok(res).toJson), Err)

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
