package org.byrde.commons.utils

object Cookies {
  type CookieName = String

  val Host: CookieName =
    "host"

  val TLSSessionInfo: CookieName =
    "tls-session-info"

  val RemoteAddress: CookieName =
    "remote-address"

  val proxyHeadersFilter: Seq[CookieName] =
    Seq(
      TLSSessionInfo,
      RemoteAddress)
}
