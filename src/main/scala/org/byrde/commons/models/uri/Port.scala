package org.byrde.commons.models.uri

case class Port(port: Int) extends AnyVal {
  override def toString: String =
    port.toString
}
