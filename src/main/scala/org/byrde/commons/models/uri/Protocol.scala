package org.byrde.commons.models.uri

case class Protocol(protocol: String) extends AnyVal

object Protocol {
  lazy val http = new Protocol("http://")
  lazy val https = new Protocol("https://")
}