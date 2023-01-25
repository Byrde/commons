package org.byrde.commons.types.uri

case class Port(port: Int) extends AnyVal {
  override def toString: String = port.toString
}
