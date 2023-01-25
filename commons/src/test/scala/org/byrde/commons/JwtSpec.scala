package org.byrde.commons

import org.byrde.commons.JwtSupport
import org.byrde.commons.types.jwt.Jwt
import org.byrde.commons.types.jwt.conf.JwtConfig
import org.byrde.commons.types.jwt.validation.JwtValidationError._

import io.circe.generic.auto._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.JwtAlgorithm

class JwtSpec extends AnyFlatSpec with JwtSupport with Matchers {
  private case class Claims(org: String, typ: String, loc: String, acc: String)

  private val claims = Claims("test", "test", "test", "test")

  "JwtSupport.toJwt" should "encode correctly encode/decode" in {
    val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config)
    Jwt(token).validate[Claims](config) shouldBe Right(claims)
  }

  it should "succeed if the subject is provided and matches" in {
    val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config, subject = Some("test"))
    Jwt(token).validate[Claims](
      config,
      subject = Some("test"),
    ) shouldBe Right(claims)
  }

  "Jwt.validate" should "fail on expired token" in {
    val config: JwtConfig = JwtConfig("test", -1L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config)
    val validationError = Jwt(token).validate[Claims](config).swap.toOption.get.asInstanceOf[Expired]
    validationError shouldBe Expired(validationError.message)
  }

  it should "fail on invalid token" in {
    val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config.copy(secret = "test1"))
    val validationError = Jwt(token).validate[Claims](config).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }

  it should "fail if the issuer does not match" in {
    val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config.copy(issuer = "test1"))
    val validationError = Jwt(token).validate[Claims](config).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }

  it should "fail if the subject is provided but does not match" in {
    val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val token = claims.toJwt(config, subject = Some("test"))
    val validationError =
      Jwt(token).validate[Claims](config, subject = Some("test1")).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }
}
