package org.byrde.jwt.support

import org.byrde.jwt.claims.Claims
import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.support.validation.JwtValidationError

import io.circe.Decoder

import pdi.jwt.JwtCirce
import pdi.jwt.exceptions._

import scala.util.{Failure, Success}

trait JwtSupport {
  def encode(claims: Claims[_])(implicit config: JwtConfig): String =
    JwtCirce.encode(claims.toJwtClaim, config.signature, config.encryptionAlgorithm)

  def decode[T](token: String)(implicit decoder: Decoder[T], config: JwtConfig): Either[JwtValidationError, T] =
    JwtCirce
      .decodeJson(token, config.signature, Seq(config.encryptionAlgorithm))
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

object JwtSupport extends JwtSupport
