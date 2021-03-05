package org.byrde.jwt

import org.byrde.jwt.conf.JwtConfig
import org.byrde.jwt.support.JwtSupport
import org.byrde.jwt.support.validation.JwtValidationError.{Expired, Invalid}

import io.circe.generic.auto._
import io.circe.generic.semiauto._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.JwtAlgorithm

class JwtSpec extends AnyFlatSpec with Matchers {
  private case class Claims(org: String, typ: String, loc: String, acc: String)

  private val claims = Claims("test", "test", "test", "test")

  "Jwt.encode" should "encode correctly encode/decode" in {
    implicit val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims)) shouldBe Right(claims)
  }
  
  it should "succeed if the subject is provided and matches" in {
    implicit val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims, subject = Some("test")), subject = Some("test")) shouldBe Right(claims)
  }

  "Jwt.decode" should "fail on expired token" in {
    implicit val config: JwtConfig = JwtConfig("test", -1L, JwtAlgorithm.HS256, "test")
    val validationError = JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims)).swap.toOption.get.asInstanceOf[Expired]
    validationError shouldBe Expired(validationError.message)
  }

  it should "fail on invalid token" in {
    implicit val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val validationError = JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims)(config.copy(secret = "test1"), deriveEncoder[Claims])).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }
  
  it should "fail if the issuer does not match" in {
    implicit val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val validationError = JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims)(config.copy(issuer = "test1"), deriveEncoder[Claims])).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }
  
  it should "fail if the subject is provided but does not match" in {
    implicit val config: JwtConfig = JwtConfig("test", 5000L, JwtAlgorithm.HS256, "test")
    val validationError = JwtSupport.validateJwt[Claims](JwtSupport.issueJwt(claims, subject = Some("test")), subject = Some("test1")).swap.toOption.get.asInstanceOf[Invalid]
    validationError shouldBe Invalid(validationError.message)
  }
}
