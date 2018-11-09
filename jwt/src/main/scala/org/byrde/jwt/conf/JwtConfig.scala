package org.byrde.jwt.conf

import org.byrde.jwt.definitions._

import org.apache.commons.codec.binary.Base64

import io.igl.jwt._

import play.api.Configuration

case class JwtConfig(tokenName: String,
                     private val secret: String,
                     encryptionAlgorithm: Algorithm,
                     headers: Set[HeaderField],
                     claims: Set[ClaimField],
                     saltOpt: Option[String] = None) {
  lazy val signature: String =
    saltOpt.fold(secret) { salt =>
      Base64.encodeBase64URLSafeString(
        secret.getBytes("UTF-8") ++ salt.getBytes("UTF-8"))
    }
}

object JwtConfig {
  def apply(config: Configuration): JwtConfig =
    apply("token", "signature", "encryption", "claims", config)

  def apply(_token: String,
            _signature: String,
            _encryption: String,
            _claims: String,
            config: Configuration): JwtConfig = {
    val token =
      config
        .get[String](_token)

    val signature =
      config
        .get[String](_signature)

    val encryption =
      config
        .get[String](_encryption)

    val claims =
      config
        .getOptional[Seq[String]](_claims)
        .getOrElse(Seq.empty[String])

    build(token, signature, encryption, claims = claims)
  }

  private def build(token: String,
                    signature: String,
                    encryption: String,
                    headers: Seq[String] = Seq.empty,
                    claims: Seq[String] = Seq.empty): JwtConfig = {
    val resolvedEncryption = encryption match {
      case "HS256" =>
        Algorithm.HS256

      case "HS384" =>
        Algorithm.HS384

      case "HS512" =>
        Algorithm.HS512

      case _ =>
        Algorithm.HS256
    }

    val resolvedClaims: Seq[ClaimField] = claims.map {
      case "sub" =>
        Sub

      case "aud" =>
        Aud

      case Admin.name =>
        Admin

      case Org.name =>
        Org

      case Name.name =>
        Name

      case Token.name =>
        Token

      case Type.name =>
        Type

      case "exp" =>
        Exp
    }

    JwtConfig(
      token,
      signature,
      resolvedEncryption,
      Set.empty[HeaderField],
      resolvedClaims.toSet)
  }
}
