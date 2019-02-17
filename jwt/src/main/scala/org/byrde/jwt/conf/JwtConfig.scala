package org.byrde.jwt.conf

import org.byrde.jwt.definitions._

import com.typesafe.config.Config
import org.apache.commons.codec.binary.Base64
import io.igl.jwt._

import scala.util.Try
import scala.collection.JavaConverters._

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
  def apply(config: Config): JwtConfig =
    apply("token", "signature", "encryption", "claims", config)

  def apply(_token: String,
            _signature: String,
            _encryption: String,
            _claims: String,
            config: Config): JwtConfig = {
    val token =
      config
        .getString(_token)

    val signature =
      config
        .getString(_signature)

    val encryption =
      config
        .getString(_encryption)

    val claims =
      Try(config.getStringList(_claims))
        .map(_.asScala)
        .getOrElse(Seq.empty[String])

    build(token, signature, encryption, claims = claims)
  }

  private def build(token: String,
                    signature: String,
                    encryption: String,
                    headers: Seq[String] = Seq.empty,
                    claims: Seq[String] = Seq.empty): JwtConfig = {
    val resolvedEncryption =
      encryption match {
        case "HS256" =>
          Algorithm.HS256

        case "HS384" =>
          Algorithm.HS384

        case "HS512" =>
          Algorithm.HS512

        case _ =>
          Algorithm.HS256
      }

    val resolvedClaims: Seq[ClaimField] =
      claims.map {
        case "sub" =>
          Sub

        case "aud" =>
          Aud

        case "exp" =>
          Exp

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
      }

    JwtConfig(
      token,
      signature,
      resolvedEncryption,
      Set.empty[HeaderField],
      resolvedClaims.toSet)
  }
}
