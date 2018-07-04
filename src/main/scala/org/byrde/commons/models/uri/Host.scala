package org.byrde.commons.models.uri

import java.net.URL

case class Host(protocol: Protocol = Protocol.http,
                host: String,
                port: Option[Port] = None) {
  override def toString: String =
    (protocol.protocol + host + port.fold("")(p => s":${p.port}")).trim
}

object Host {
  def fromString(value: String, secure: Boolean = true): Host =
    fromURL(Url.handleMissingProtocol(value, secure))

  def fromURL(url: URL): Host = {
    val port =
      if (url.getPort != -1)
        Option(url.getPort)
      else
        None

    Host(
      Protocol.fromString(url.getProtocol),
      url.getHost,
      port.map(Port.apply))
  }
}
