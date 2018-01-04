package org.byrde.commons.services.rest

import play.api.http._
import play.api.libs.json.Reads
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class RestService {
  protected val requestHolder: WSRequest
  protected val timeout: Duration

  protected def wrapRequest[A: ClassTag](body: Option[String])(req: => Future[WSResponse])(implicit ec: ExecutionContext, reads: Reads[A]): Future[A]

  def get[A: ClassTag](implicit ec: ExecutionContext, reads: Reads[A]): Future[A] = {
    wrapRequest[A](None)(requestHolder.withRequestTimeout(timeout).get())
  }

  def post[A: ClassTag, T](body: T)(implicit ec: ExecutionContext, ct: ContentTypeOf[T], reads: Reads[A]): Future[A] = {
    wrapRequest[A](Some(body.toString))(requestHolder.withRequestTimeout(timeout).post(body))
  }
}