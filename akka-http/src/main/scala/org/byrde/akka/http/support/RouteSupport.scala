package org.byrde.akka.http.support

import org.byrde.akka.http.rejections.RejectionException
import org.byrde.service.response.ServiceResponse

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route

import play.api.libs.json.Writes

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RouteSupport extends PlayJsonSupport {
  def SuccessCode: Int

  def asyncJson[T](
    fn: Future[T],
    title: String = "Success",
    code: Int = SuccessCode,
    Err: Throwable => Throwable = identity
  )(implicit writes: Writes[T]): Route =
    async(fn, (res: T) => complete(ServiceResponse(title, code, res).toJson), Err)

  def asyncServiceResponse[T <: ServiceResponse[_]](
    fn: Future[T],
    Err: Throwable => Throwable = identity
  ): Route =
    async(fn, (res: T) => complete(res.toJson), Err)

  def asyncWithDefaultJsonResponse[T, TT <: ServiceResponse[_]](
    fn: Future[T],
    Ok: TT,
    Err: Throwable => Throwable = identity
  ): Route =
    async(fn, (_: T) => complete(Ok.toJson), Err)

  def asyncWithCustomJsonResponse[T, TT <: ServiceResponse[_]](
    fn: Future[T],
    Ok: T => TT,
    Err: Throwable => Throwable = identity
  ): Route =
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
