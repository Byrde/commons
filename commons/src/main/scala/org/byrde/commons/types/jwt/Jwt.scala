package org.byrde.commons.types.jwt

import org.byrde.commons.types.jwt.conf.JwtConfig
import org.byrde.commons.types.jwt.validation.JwtValidationError

import io.circe.parser._
import io.circe.{ Decoder, DecodingFailure, ParsingFailure }

import scala.annotation.nowarn

import pdi.jwt.exceptions.{
  JwtExpirationException,
  JwtLengthException,
  JwtNotBeforeException,
  JwtSignatureFormatException,
  JwtValidationException,
}
import pdi.jwt.{ JwtCirce, JwtClaim }

case class Jwt(token: String) {
  def validate[T](
    token: String,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty,
  )(implicit decoder: Decoder[T], config: JwtConfig): Either[JwtValidationError, T] =
    JwtCirce
      .decode(
        token,
        config.signature,
        Seq(config.encryptionAlgorithm),
      )
      .toEither
      .flatMap(validateJwtClaim(subject, audience))
      .map(_.content)
      .flatMap(parse)
      .flatMap(_.as[T])
      .left
      .map {
        case ex: JwtLengthException =>
          JwtValidationError.Invalid(ex.getMessage)

        case ex: JwtValidationException =>
          JwtValidationError.Invalid(ex.getMessage)

        case ex: JwtSignatureFormatException =>
          JwtValidationError.Invalid(ex.getMessage)

        case ex: JwtExpirationException =>
          JwtValidationError.Expired(ex.getMessage)

        case ex: JwtNotBeforeException =>
          JwtValidationError.Expired(ex.getMessage)

        case ex: ParsingFailure =>
          JwtValidationError.Invalid(ex.getMessage)

        case ex: DecodingFailure =>
          JwtValidationError.Invalid(ex.getMessage)

        case exception =>
          throw exception
      }

  private def validateJwtClaim(
    @nowarn subject: Option[String] = Option.empty,
    @nowarn audience: Option[Set[String]] = Option.empty,
  )(jwtClaim: JwtClaim)(implicit config: JwtConfig): Either[Throwable, JwtClaim] = {
    val isValid =
      jwtClaim.issuer.fold(false)(_ == config.issuer) &&
        subject.fold(true)(sub => jwtClaim.subject.fold(false)(_ == sub)) &&
        audience.fold(true)(aud => jwtClaim.audience.fold(false)(_ == aud))

    if (isValid)
      Right(jwtClaim)
    else
      Left(new JwtValidationException("Invalid issuer, subject, or audience."))
  }
}
