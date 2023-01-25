package org.byrde.commons.types.uri

import java.net.URL

case class Host(
  host: String,
  protocol: Protocol = Protocol.http,
  port: Option[Port] = None,
) {
  def / (path: String): Url = Url(this, path = Path(path))

  def / (path: Path): Url = Url(this, path = path)

  def ? (query: (String, String)): Url = &(query)

  def & (query: (String, String)): Url = Url(this, Path.empty(Queries(query)))

  def ?+ (queries: Set[(String, String)]): Url = &+(queries)

  def &+ (queries: Set[(String, String)]): Url = Url(this, Path.empty(Queries(queries)))

  def withQueries(queries: Queries): Url = Url(this, Path.empty(queries))

  def toUrl: Url = Url(this, Path.empty())

  override def toString: String = (protocol.protocol + host + port.fold("")(p => s":${p.port}")).trim
}

object Host {
  def fromString(value: String, secure: Boolean = true): Host = fromURL(Url.handleMissingProtocol(value, secure))

  def fromURL(url: URL): Host = {
    val port =
      if (url.getPort != -1)
        Option(url.getPort)
      else
        None

    Host(url.getHost, Protocol.fromString(url.getProtocol), port.map(Port.apply))
  }
}
