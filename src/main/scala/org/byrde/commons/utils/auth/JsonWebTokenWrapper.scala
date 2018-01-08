package org.byrde.commons.utils.auth

import io.igl.jwt.{Alg, ClaimValue, DecodedJwt, Jwt}

import org.byrde.commons.utils.auth.conf.JwtConfig

import scala.util.Try

case class JsonWebTokenWrapper(jwtConfig: JwtConfig) {
  def encode[A](claims: Seq[ClaimValue]): String =
    new DecodedJwt(Seq(Alg(jwtConfig.encryptionAlgorithm)), claims)
      .encodedAndSigned(jwtConfig.signature)

  def decode[A](token: String): Try[Jwt] =
    DecodedJwt.validateEncodedJwt(token,
                                  jwtConfig.signature,
                                  jwtConfig.encryptionAlgorithm,
                                  jwtConfig.headers,
                                  jwtConfig.claims)
}
