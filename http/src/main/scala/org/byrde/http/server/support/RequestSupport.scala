package org.byrde.http.server.support

import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest}

import org.byrde.http.server.{Body, Header}
import org.byrde.uri.Url

import sttp.client3.{BodySerializer, Request, UriContext}
import sttp.model.Uri

trait RequestSupport {
  implicit class HttpRequest2SttpRequest(request: HttpRequest) {
    def toSttpRequest[I](
      url: Url,
      transformRequestHeaders: Seq[Header] => Seq[Header],
    ): Request[Either[String, String], Any] =
      baseRequest(url)(request.method)
        .headers(transformRequestHeaders(request.headers.map(Header.fromHttpHeader)).map(_.toSttpHeader): _*)
    
    def toSttpRequest[I](
      url: Url,
      body: Body[I],
      transformRequestHeaders: Seq[Header] => Seq[Header],
    )(implicit serializer: BodySerializer[I]): Request[Either[String, String], Any] =
      baseRequest(url)(request.method)
        .body(body.value)
        .headers(transformRequestHeaders(request.headers.map(Header.fromHttpHeader)).map(_.toSttpHeader): _*)
    
    private def baseRequest(url: Url): HttpMethod => Request[Either[String, String], Any] = {
      case HttpMethods.GET =>
        sttp.client3.basicRequest.get(url)
  
      case HttpMethods.POST =>
        sttp.client3.basicRequest.post(url)
  
      case HttpMethods.PUT =>
        sttp.client3.basicRequest.put(url)
  
      case HttpMethods.PATCH =>
        sttp.client3.basicRequest.patch(url)
  
      case HttpMethods.DELETE =>
        sttp.client3.basicRequest.delete(url)
    }
  
    private implicit def url2Uri(url: Url): Uri =
      uri"${url.toString}"
  }
}
