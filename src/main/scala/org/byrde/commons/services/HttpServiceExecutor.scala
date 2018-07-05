package org.byrde.commons.services

import org.byrde.commons.models.uri.{Host, Path, Url}
import org.byrde.commons.utils.Cookies
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

  def underlyingGet(path: Path, requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path)).withMethod("GET"))

  def underlyingPost[T](body: T)(path: Path, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path)).withBody(body).withMethod("POST"))

  def underlyingPut[T](body: T)(path: Path, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path)).withBody(body).withMethod("PUT"))

  def underlyingDelete(path: Path, requestBuilder: WSRequest => WSRequest = identity): Future[WSResponse] =
    executeRequest(requestBuilder(buildWSRequest(path)).withMethod("DELETE"))

  def proxy[T](path: Path, requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T], request: Request[T]): Future[WSResponse] = {
    val proxyRequest =
      request.toWSRequest(buildWSRequest(path))

    val proxyHeaders =
      proxyRequest
        .headers
        .map {
          case (key, value) if key.equalsIgnoreCase(Cookies.Host) =>
            key -> Seq(host.host.toString)
          case (key, value) =>
            key -> value
        }
        .filterNot {
          case (key, _) =>
            Cookies.proxyHeadersFilter.contains(key.toLowerCase)
        }

    val proxyRequestWithProxyHeaders =
      proxyRequest
        .withHttpHeaders(proxyHeaders.mapValues(_.mkString(",")).toSeq: _*)

    executeRequest(requestBuilder(proxyRequestWithProxyHeaders))
  }

  private def buildWSRequest(path: Path): WSRequest =
    client.url(Url(host, path).toString)
}
