package org.byrde.commons.services

import org.byrde.commons.models.uri.{Host, Path, Url}
import org.byrde.commons.utils.RequestUtils._

import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import play.api.mvc.Request

import scala.concurrent.Future

trait HttpServiceExecutor {
  val name: String = {
    val clazz =
      this.getClass

    if (clazz.isLocalClass)
      clazz.getGenericSuperclass.getTypeName
    else
      clazz.getSimpleName
  }

  def host: Host

  def client: WSClient

  def executeRequest(request: WSRequest): Future[WSResponse]

  def underlyingGet(path: Path, secure: Boolean = true, requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path, secure)).withMethod("GET"))

  def underlyingPost[T](body: T)(path: Path, secure: Boolean = true, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path, secure)).withBody(body).withMethod("POST"))

  def underlyingPut[T](body: T)(path: Path, secure: Boolean = true, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path, secure)).withBody(body).withMethod("PUT"))

  def underlyingDelete(path: Path, secure: Boolean = true, requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path, secure)).withMethod("DELETE"))

  def proxy[T](path: Path, secure: Boolean = true, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T], request: Request[T]): Future[WSResponse] =
    executeRequest(requestBuilder(request.toWSRequest(buildWSRequest(path, secure))))

  private def buildWSRequest(path: Path, secure: Boolean): WSRequest =
    client.url(Url(host, path).toString)
}
