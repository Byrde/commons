package org.byrde.utils

import org.byrde.uri.Host
import org.byrde.utils.OptionUtils._

import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, WSCookie}

object RequestUtils {
  implicit class Request2WSRequest[T](value: play.api.mvc.Request[T]) {
    @inline def toWSRequest(base: StandaloneWSRequest, newHost: Option[Host] = None)(implicit bodyWritable: BodyWritable[T]): StandaloneWSRequest =
      base
        .withBody(value.body)
        .withMethod(value.method)
        .withHttpHeaders(value.headers.toHeaderSeq(newHost): _*)
        .withCookies(value.cookies.map(_.toWSCookie).toSeq: _*)
  }

  implicit class Headers2HeaderSeq(value: play.api.mvc.Headers) {
    @inline def toHeaderSeq(newHost: Option[Host] = None): Seq[(String, String)]  =
      value
        .headers
        .flatMap {
          case (headerKey, _) if Headers.proxyHeadersFilter.contains(headerKey.toLowerCase) =>
            None

          case (headerKey, _) if newHost.nonEmpty && headerKey.equalsIgnoreCase(Headers.Host) =>
            Some(headerKey -> newHost.get.host.toString)

          case (headerKey, headerValue) =>
            Some(headerKey -> headerValue)
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
