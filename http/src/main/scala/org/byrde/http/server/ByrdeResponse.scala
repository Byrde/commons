package org.byrde.http.server

trait ByrdeResponse {
  def code: Int
}

object ByrdeResponse {
  case class Default(message: String, code: Int) extends ByrdeResponse
}