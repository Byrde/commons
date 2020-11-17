package org.byrde.tapir

trait TapirResponse {
  def code: Int
}

object TapirResponse {
  case class Default(code: Int) extends TapirResponse
}