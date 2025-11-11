package org.byrde.commons.types.uri

import java.net.{ MalformedURLException, URI, URL }

import scala.util.control.NonFatal

case class Url(host: Host, path: Path) {
  def / (newPath: String): Url = copy(path = path / newPath)

  def / (newPath: Path): Url = copy(path = path / newPath)

  def &+ (query: (String, String)): Url = copy(path = path &+ query)

  def &+ (query: Option[(String, String)]): Url = copy(path = path &+ query)

  def &+ (_queries: Queries): Url = copy(path = path &+ _queries)

  override def toString: String = (host.toString + path.toString).trim
}

object Url {
  def fromString(value: String, secure: Boolean = true): Url = {
    val url = handleMissingProtocol(value, secure)

    val host = Host.fromURL(url)

    val path = Path.fromURL(url)

    Url(host, path)
  }

  def handleMissingProtocol(value: String, secure: Boolean): URL =
    try
      URI.create(value).toURL
    catch {
      case NonFatal(ex)
        if value.nonEmpty && ex.isInstanceOf[MalformedURLException] && ex.getMessage.startsWith("no protocol") =>
        val protocol =
          if (secure)
            Protocol.https
          else
            Protocol.http

        URI.create(protocol.toString + value).toURL
    }
}
