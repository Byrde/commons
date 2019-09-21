package org.byrde.clients.ahc

import org.byrde.clients.ahc.conf.ServiceConfig
import org.byrde.uri.{Host, Path, Url}
import org.byrde.clients.utils.RequestSupport._

import play.api.libs.ws.{BodyWritable, StandaloneWSClient, StandaloneWSRequest, StandaloneWSResponse}
import play.api.mvc.Request

import scala.concurrent.Future

trait AhcExecutor {
  type CurlRequest = String

  val name: String = {
    val clazz =
      this.getClass

    if (clazz.isLocalClass)
      clazz.getGenericSuperclass.getTypeName
    else
      clazz.getSimpleName
  }

  val host: Host =
    config.host

  def config: ServiceConfig

  def client: StandaloneWSClient

  def executeRequest(request: StandaloneWSRequest): Future[StandaloneWSResponse]

  def underlyingGet(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity): Future[StandaloneWSResponse] =
    executeRequest(requestHook(buildWSRequest(path)).withMethod("GET"))

  def underlyingPost[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[StandaloneWSResponse] =
    executeRequest(requestHook(buildWSRequest(path)).withBody(body).withMethod("POST"))

  def underlyingPut[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[StandaloneWSResponse] =
    executeRequest(requestHook(buildWSRequest(path)).withBody(body).withMethod("PUT"))

  def underlyingDelete(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity): Future[StandaloneWSResponse] =
    executeRequest(requestHook(buildWSRequest(path)).withMethod("DELETE"))

  def underlyingPatch[T](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit bodyWritable: BodyWritable[T]): Future[StandaloneWSResponse] =
    executeRequest(requestHook(buildWSRequest(path)).withBody(body).withMethod("PATCH"))

  def proxy[T](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity)(implicit bodyWritable: BodyWritable[T], request: Request[T]): Future[StandaloneWSResponse] =
    executeRequest(requestHook(request.toWSRequest(buildWSRequest(path), Some(host))))

  private def buildWSRequest(path: Path): StandaloneWSRequest =
    client.url(Url(host, path).toString)
}
