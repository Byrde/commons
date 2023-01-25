package org.byrde.commons.types.jwt

import org.byrde.commons.types.jwt.conf.JwtConfig

import io.circe.Encoder
import io.circe.syntax._

import java.time.Instant

import pdi.jwt.{ JwtCirce, JwtClaim }

trait JwtSupport {
  implicit class Any2Jwt[T](value: T) {
    def toJwt(
      subject: Option[String] = Option.empty,
      audience: Option[Set[String]] = Option.empty,
    )(implicit encoder: Encoder[T], config: JwtConfig): String =
      JwtCirce.encode(
        JwtClaim(
          content = value.asJson.noSpaces,
          subject = subject,
          audience = audience,
          issuer = Some(config.issuer),
          expiration = Some(Instant.now.getEpochSecond + config.expirationSeconds),
        ),
        config.signature,
        config.encryptionAlgorithm,
      )
  }
}
