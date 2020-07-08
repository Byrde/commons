package org.byrde.jwt

import org.byrde.jwt.claims.Claims
import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.support.JwtSupport
import org.byrde.jwt.support.validation.JwtValidationError.{Expired, Invalid}

import java.time.Instant

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.generic.auto._

import pdi.jwt.JwtAlgorithm

class JwtSpec extends AnyFlatSpec with Matchers {
  private case class Claim(sub: String, org: String, typ: String, loc: String, acc: String)

  private implicit val config: JwtConfig =
    JwtConfig("test", "test", JwtAlgorithm.HS256)

  private val claim = Claim("test", "test", "test", "test", "test")

  "Jwt.encode" should "encode correctly encode/decode" in {
    val claims = Claims(expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond), claim = Some(claim))

    JwtSupport.decode[Claim](JwtSupport.encode(claims)) shouldBe Right(claim)
  }

  "Jwt.decode" should "fail on expired token" in {
    val claims = Claims(expiration = Some(Instant.now.minusSeconds(3600).getEpochSecond), claim = Some(claim))

    val validationError = JwtSupport.decode[Claim](JwtSupport.encode(claims)).swap.toOption.get.asInstanceOf[Expired]

    validationError shouldBe Expired(validationError.message)
  }

  it should "fail on invalid token" in {
    val claims = Claims(expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond), claim = Some(claim))

    val config1: JwtConfig = config.copy(secret = "test1")
    val validationError = JwtSupport.decode[Claim](JwtSupport.encode(claims)(config1)).swap.toOption.get.asInstanceOf[Invalid]

    validationError shouldBe Invalid(validationError.message)
  }
}
