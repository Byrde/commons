package org.byrde.jwt

import io.circe.Decoder

import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.validation.JwtValidationError

import pdi.jwt.exceptions.{JwtExpirationException, JwtLengthException, JwtNotBeforeException, JwtSignatureFormatException, JwtValidationException}
import pdi.jwt.{JwtCirce, JwtClaim}

import scala.util.{Failure, Success}

case class Jwt(jwtConfig: JwtConfig) {
  def encode[T](claims: JwtClaim): String =
    JwtCirce.encode(claims, jwtConfig.signature, jwtConfig.encryptionAlgorithm)

  def decode[T](token: String)(implicit decoder: Decoder[T]): Either[JwtValidationError, T] =
    JwtCirce
      .decodeJson(token)
      .flatMap(_.as[T].toTry) match {
        case Success(value) =>
          Right(value)

        case Failure(ex: JwtLengthException) =>
          Left(JwtValidationError.Invalid(ex.getMessage))

        case Failure(ex: JwtValidationException) =>
          Left(JwtValidationError.Invalid(ex.getMessage))

        case Failure(ex: JwtSignatureFormatException) =>
          Left(JwtValidationError.Invalid(ex.getMessage))

        case Failure(ex: JwtExpirationException) =>
          Left(JwtValidationError.Expired(ex.getMessage))

        case Failure(ex: JwtNotBeforeException) =>
          Left(JwtValidationError.Expired(ex.getMessage))

        case Failure(exception) =>
          throw exception
      }
}