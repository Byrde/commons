package org.byrde.client.http.play

object Headers {

  type HeaderName = String

  val Host: HeaderName =
    "host"

  val TLSSessionInfo: HeaderName =
    "tls-session-info"

  val RemoteAddress: HeaderName =
    "remote-address"

  val proxyHeadersFilter: Seq[HeaderName] =
    Seq(
      TLSSessionInfo,
      RemoteAddress
    )

}
