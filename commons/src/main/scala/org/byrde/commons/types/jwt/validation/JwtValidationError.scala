package org.byrde.commons.types.jwt.validation

sealed trait JwtValidationError

object JwtValidationError {
  case class Invalid(message: String) extends JwtValidationError

  case class Expired(message: String) extends JwtValidationError
}
