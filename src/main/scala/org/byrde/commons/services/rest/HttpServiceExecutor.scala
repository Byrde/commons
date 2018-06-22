package org.byrde.commons.services.rest

import org.byrde.commons.utils.RequestUtils._

import play.api.libs.ws.{BodyWritable, WSRequest, WSResponse}
import play.api.mvc.Request

import scala.concurrent.Future

trait HttpServiceExecutor {
  def connection: WSRequest

  val name: String = {
    val clazz =
      this.getClass

    if (clazz.isLocalClass)
      clazz.getGenericSuperclass.getTypeName
    else
      clazz.getSimpleName
  }

  def executeRequest(request: WSRequest): Future[WSResponse]

  def underlyingGet(requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(connection).withMethod("GET"))

  def underlyingPost[T](body: T)(requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(connection).withBody(body).withMethod("POST"))

  def underlyingPut[T](body: T)(requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(connection).withBody(body).withMethod("PUT"))

  def underlyingDelete(requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(connection).withMethod("DELETE"))

  def proxy[T](requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T], request: Request[T]): Future[WSResponse] =
    executeRequest(requestBuilder(request.toWSRequest(connection)))
}
