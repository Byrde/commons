package org.byrde.commons.services

import org.byrde.commons.models.uri.{Host, Path, Url}
import org.byrde.commons.utils.RequestUtils._

import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import play.api.mvc.Request

import scala.concurrent.Future

trait HttpServiceExecutor {
  type CurlRequest = String

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

  def underlyingGet(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[WSResponse] =
    executeRequest(requestHook(buildWSRequest(path, curlRequestHook)).withMethod("GET"))

  def underlyingPost[T](body: T)(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestHook(buildWSRequest(path, curlRequestHook)).withBody(body).withMethod("POST"))

  def underlyingPut[T](body: T)(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit bodyWritable: BodyWritable[T]): Future[WSResponse] =
    executeRequest(requestHook(buildWSRequest(path, curlRequestHook)).withBody(body).withMethod("PUT"))

  def underlyingDelete(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ()): Future[WSResponse] =
    executeRequest(requestHook(buildWSRequest(path, curlRequestHook)).withMethod("DELETE"))

  def proxy[T](path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => ())(implicit bodyWritable: BodyWritable[T], request: Request[T]): Future[WSResponse] =
    executeRequest(requestHook(request.toWSRequest(buildWSRequest(path, curlRequestHook), Some(host))))

  private def buildWSRequest(path: Path, curlRequestHook: CurlRequest => Unit): WSRequest =
    client.url(Url(host, path).toString).withRequestFilter(new AhcCurlRequestFilter(curlRequestHook))
}
