package org.byrde.jwt.support

import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.support.validation.JwtValidationError

import java.time.Instant

import io.circe.syntax._
import io.circe.parser._
import io.circe.{Decoder, DecodingFailure, Encoder, ParsingFailure}

import pdi.jwt.exceptions._
import pdi.jwt.{JwtCirce, JwtClaim}

trait JwtSupport {
  def issueJwt[T](
    content: T,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit config: JwtConfig, encoder: Encoder[T]): String =
    JwtCirce.encode(
      JwtClaim(
        content = content.asJson.noSpaces,
        subject = subject,
        audience = audience,
        issuer = Some(config.issuer),
        expiration = Some(Instant.now.getEpochSecond + config.expirationSeconds)
      ),
      config.signature,
      config.encryptionAlgorithm
    )

  def validateJwt[T](
    token: String,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit config: JwtConfig, decoder: Decoder[T]): Either[JwtValidationError, T] =
    JwtCirce
      .decode(
        token,
        config.signature,
        Seq(config.encryptionAlgorithm)
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
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
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

object JwtSupport extends JwtSupport
