package org.byrde.uri

case class Protocol(protocol: String) extends AnyVal {
  override def toString: String =
    protocol
}

object Protocol {
  lazy val http  =
    new Protocol("http://")

  lazy val https =
    new Protocol("https://")

  def fromString: String => Protocol = {
    case "https" =>
      https
    case _ =>
      http
  }
}