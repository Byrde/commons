package org.byrde.jwt.support.validation

sealed trait JwtValidationError

object JwtValidationError {
  case class Invalid(message: String) extends JwtValidationError

  case class Expired(message: String) extends JwtValidationError
}
