package org.byrde.http.server

trait Response {
  def message: String
}

object Response {
  case object Ack extends Response {
    override def message: String = "Success"
  }
  
  case class Default(message: String) extends Response
}