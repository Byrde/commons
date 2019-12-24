package org.byrde.jwt.conf

import com.typesafe.config.Config

import pdi.jwt.{JwtAlgorithm, JwtClaim}

case class JwtConfig(
  tokenName: String,
  private val secret: String,
  encryptionAlgorithm: JwtAlgorithm,
  saltOpt: Option[String] = None
) {
  lazy val signature: String =
    saltOpt.fold(secret)(_ + "_" + secret)
}

object JwtConfig {
  def apply(config: Config): JwtConfig =
    apply("token", "signature", "encryption", config)

  def apply(
    _token: String,
    _signature: String,
    _encryption: String,
    config: Config
  ): JwtConfig = {
    val token =
      config
        .getString(_token)

    val signature =
      config
        .getString(_signature)

    val encryption =
      config
        .getString(_encryption)

    build(token, signature, encryption)
  }

  private def build[T](
    token: String,
    signature: String,
    encryption: String,
  ): JwtConfig = {
    val resolvedEncryption =
      encryption match {
        case "HS256" =>
          JwtAlgorithm.HS256

        case "HS384" =>
          JwtAlgorithm.HS384

        case "HS512" =>
          JwtAlgorithm.HS512

        case _ =>
          JwtAlgorithm.HS256
      }

    JwtClaim

    JwtConfig(
      token,
      signature,
      resolvedEncryption,
    )
  }
}
