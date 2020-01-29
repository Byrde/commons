package org.byrde.support

import org.byrde.support.types.Email
import org.byrde.support.types.Email.EmailInvalid

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EmailSpec extends AnyFlatSpec with Matchers {
  "Email.fromString" should "extract email parts" in {
    val email = Email.fromString("martin@simple.reviews").toOption.get
    email.recipient shouldBe "martin"
    email.domain shouldBe "simple"
    email.domainSuffix shouldBe "reviews"
    email.toString shouldBe "martin@simple.reviews"
  }

  it should "allow multi tiered domains" in {
    val email = Email.fromString("test@big.com.au").toOption.get
    email.recipient shouldBe "test"
    email.domain shouldBe "big"
    email.domainSuffix shouldBe "com.au"
    email.toString shouldBe "test@big.com.au"
  }

  it should "allow special characters" in {
    val email = Email.fromString("+martin@simple.reviews").toOption.get
    email.recipient shouldBe "+martin"
    email.domain shouldBe "simple"
    email.domainSuffix shouldBe "reviews"
  }

  it should "fail on duplicate @" in {
    Email.fromString("martin@@simple.reviews") shouldBe Left(EmailInvalid)
  }

  it should "fail on missing @" in {
    Email.fromString("martinsimple.reviews") shouldBe Left(EmailInvalid)
  }

  it should "fail on missing domain suffix" in {
    Email.fromString("martin@simplereviews") shouldBe Left(EmailInvalid)
  }

  it should "fail on missing all required tokens" in {
    Email.fromString("martinsimplereviews") shouldBe Left(EmailInvalid)
  }
}
