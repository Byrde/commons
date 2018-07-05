package org.byrde.commons.utils

import OptionUtils._
import org.byrde.commons.models.uri.Host

import play.api.libs.ws.{BodyWritable, WSCookie, WSRequest}
import play.api.mvc.{Cookie, Request}

object RequestUtils {
  implicit class Request2WSRequest[T](value: Request[T]) {
    @inline def toWSRequest(base: WSRequest, newHost: Option[Host] = None)(implicit bodyWritable: BodyWritable[T]): WSRequest = {
      val filteredProxyHeaders =
        base
          .headers
          .flatMap {
            case (headerKey, _) if Headers.proxyHeadersFilter.contains(headerKey.toLowerCase) =>
              None
            case (headerKey, _) if newHost.nonEmpty && headerKey.equalsIgnoreCase(Headers.Host) =>
              Some(headerKey -> newHost.get.host.toString)
            case (headerKey, headerValue) =>
              Some(headerKey -> headerValue.mkString(", "))
          }
          .toSeq

      base
        .withBody(value.body)
        .withMethod(value.method)
        .withHttpHeaders(filteredProxyHeaders: _*)
        .withCookies(value.cookies.map(_.toWSCookie).toSeq: _*)
    }
  }

  implicit class Cookie2WSCookie(value: Cookie) {
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
