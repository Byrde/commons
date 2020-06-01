package org.byrde.client.http.play.support

import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, WSCookie}

import org.byrde.client.http.play.Headers
import org.byrde.support.TypeSupport
import org.byrde.uri.Host

trait ProxyRequestSupport extends TypeSupport {

  implicit class Request2WSRequest[T](value: play.api.mvc.Request[T]) {
    
    @inline def toWSRequest(
      request: StandaloneWSRequest,
      host: Option[Host] = Option.empty
    )(implicit bodyWritable: BodyWritable[T]): StandaloneWSRequest =
      request
        .withBody(value.body)
        .withMethod(value.method)
        .withHttpHeaders(value.headers.toHeaderSeq(host): _*)
        .withCookies(value.cookies.map(_.toWSCookie).toSeq: _*)
    
  }

  implicit class Headers2HeaderSeq(value: play.api.mvc.Headers) {
    
    @inline def toHeaderSeq(host: Option[Host] = None): Seq[(String, String)] =
      value.headers.collect {
        case (key, _) if host.nonEmpty && key.equalsIgnoreCase(Headers.Host) =>
          key -> host.get.host.toString

        case (key, value) if !Headers.proxyHeadersFilter.contains(key.toLowerCase) =>
          key -> value
      }
    
  }

  implicit class Cookie2WSCookie(value: play.api.mvc.Cookie) {
    self =>

    @inline def toWSCookie: WSCookie =
      new WSCookie {
        override def name: String =
          self.value.name

        override def value: String =
          self.value.value

        override def domain: Option[String] =
          self.value.domain

        override def path: Option[String] =
          self.value.path.?

        override def maxAge: Option[Long] =
          self.value.maxAge.map(_.toLong)

        override def secure: Boolean =
          self.value.secure

        override def httpOnly: Boolean =
          self.value.httpOnly
      }
  }

}

object ProxyRequestSupport extends ProxyRequestSupport
