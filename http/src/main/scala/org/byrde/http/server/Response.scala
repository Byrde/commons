package org.byrde.http.server

trait Response {
  def code: Int
}

object Response {
  case class Default(message: String, code: Int) extends Response
}