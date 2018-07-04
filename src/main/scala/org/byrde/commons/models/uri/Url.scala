package org.byrde.commons.models.uri

import java.net.{MalformedURLException, URL}

import scala.util.control.NonFatal

case class Url(host: Host, path: Path) {
  def /(newPath: String): Url =
    copy(path = path / newPath)

  def /(newPath: Path): Url =
    copy(path = path / newPath)

  def +(newQuery: (String, String)): Url =
    copy(path = path + newQuery)

  def ++(newQuery: Set[(String, String)]): Url =
    copy(path = path ++ newQuery)

  def withQueries(newQuery: Queries): Url =
    copy(path = path withQueries newQuery)

  override def toString: String =
    (host.toString + path.toString).trim
}

object Url {
  def fromString(value: String, secure: Boolean = true): Url = {
    val url =
      handleMissingProtocol(value, secure)

    val host =
      Host.fromURL(url)

    val path =
      Path.fromURL(url)

    Url(host, path)
  }

  def handleMissingProtocol(value: String, secure: Boolean): URL =
    try {
      new URL(value)
    } catch {
      case NonFatal(ex) if value.nonEmpty && ex.isInstanceOf[MalformedURLException] && ex.getMessage.startsWith("no protocol")=>
        val protocol =
          if (secure)
            Protocol.https
          else
            Protocol.http

        new URL(protocol + value)
    }
}
