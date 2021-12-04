package org.byrde.http.server

import java.util.UUID

trait ErrorResponse {
  def code: UUID
}

object ErrorResponse {
  case class Default(code: UUID) extends ErrorResponse
}
