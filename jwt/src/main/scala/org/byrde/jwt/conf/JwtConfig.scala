package org.byrde.jwt.conf

import com.typesafe.config.Config

import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtHmacAlgorithm

case class JwtConfig(
  issuer: String,
  expirationSeconds: Long,
  encryptionAlgorithm: JwtHmacAlgorithm,
  private val secret: String,
  saltOpt: Option[String] = None,
) {
  def withSalt(salt: String): JwtConfig = copy(saltOpt = Some(salt))

  lazy val signature: String = saltOpt.fold(secret)(_ + "_" + secret)
}

object JwtConfig {
  def apply(config: Config): JwtConfig =
    apply(
      "issuer",
      "expiration",
      "encryption",
      "secret",
      config,
    )

  def apply(
    _issuer: String,
    _expiration: String,
    _encryption: String,
    _secret: String,
    config: Config,
  ): JwtConfig = {
    val issuer = config.getString(_issuer)

    val expiration = config.getLong(_expiration)

    val encryption = config.getString(_encryption)

    val secret = config.getString(_secret)

    build(issuer, expiration, encryption, secret)
  }

  private def build[T](
    issuer: String,
    expiration: Long,
    _encryption: String,
    secret: String,
  ): JwtConfig = {
    val encryption =
      _encryption match {
        case "HS256" =>
          JwtAlgorithm.HS256

        case "HS384" =>
          JwtAlgorithm.HS384

        case "HS512" =>
          JwtAlgorithm.HS512

        case _ =>
          JwtAlgorithm.HS256
      }

    JwtConfig(
      issuer,
      expiration,
      encryption,
      secret,
    )
  }
}
