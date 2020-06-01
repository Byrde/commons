package org.byrde.uri

import java.net.URL

case class Host(
  host: String,
  protocol: Protocol = Protocol.http,
  port: Option[Port] = None
) {
  
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
      url.getHost,
      Protocol.fromString(url.getProtocol),
      port.map(Port.apply))
  }
  
}
