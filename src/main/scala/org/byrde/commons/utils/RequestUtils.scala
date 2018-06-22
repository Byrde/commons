package org.byrde.commons.utils

import OptionUtils._

import play.api.libs.ws.{BodyWritable, WSCookie, WSRequest}
import play.api.mvc.{Cookie, Request}

object RequestUtils {
  implicit class Request2WSRequest[T](value: Request[T]) {
    def toWSRequest(base: WSRequest)(implicit bodyWritable: BodyWritable[T]): WSRequest =
      base
        .withBody(value.body)
        .withMethod(value.method)
        .withHttpHeaders(value.headers.headers: _*)
        .withCookies(value.cookies.map(_.toWSCookie).toSeq: _*)
  }

  implicit class Cookie2WSCookie(value: Cookie) {
    self =>

    def toWSCookie: WSCookie =
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
