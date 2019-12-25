package org.byrde.service.response

import java.time.Instant

import io.circe.generic.auto._
import org.byrde.jwt.Jwt
import org.byrde.jwt.claims.Claims
import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.validation.JwtValidationError.{Expired, Invalid}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.JwtAlgorithm

class JwtSpec extends AnyFlatSpec with Matchers {
  private case class Claim(sub: String, org: String, typ: String, loc: String, acc: String)

  private val jwt: Jwt =
    Jwt(JwtConfig("test", "test", JwtAlgorithm.HS256))

  private val claim = Claim("test", "test", "test", "test", "test")

  "Jwt.encode" should "encode correctly encode/decode" in {
    val claims = Claims(expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond), claim = Some(claim))

    jwt.decode[Claim](jwt.encode(claims)) shouldBe Right(claim)
  }

  "Jwt.decode" should "fail on expired token" in {
    val claims = Claims(expiration = Some(Instant.now.minusSeconds(3600).getEpochSecond), claim = Some(claim))

    val validationError = jwt.decode[Claim](jwt.encode(claims)).swap.toOption.get.asInstanceOf[Expired]

    validationError shouldBe Expired(validationError.message)
  }

  it should "fail on invalid token" in {
    val claims = Claims(expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond), claim = Some(claim))

    val jwt1 = Jwt(jwt.jwtConfig.copy(secret = "test1"))
    val validationError = jwt1.decode[Claim](jwt.encode(claims)).swap.toOption.get.asInstanceOf[Invalid]

    validationError shouldBe Invalid(validationError.message)
  }
}
