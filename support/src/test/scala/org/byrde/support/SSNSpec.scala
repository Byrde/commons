package org.byrde.support

import SSN._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SSNSpec extends AnyFlatSpec with Matchers {
  "SSN" should "should work for valid full SSN" in {
    SSN.fromString("899-13-1234") shouldEqual Right(SSN.Full("899", "13", "1234"))
  }

  it should "should work for valid full SSN - no hyphens" in {
    SSN.fromString("899131234") shouldEqual Right(SSN.Full("899", "13", "1234"))
  }

  it should "should work for last four SSN" in {
    SSN.fromString("1234") shouldEqual Right(SSN.LastFour("1234"))
  }

  it should "not work for full SSN with short first segment" in {
    SSN.fromString("89-13-1234") shouldEqual Left(InvalidSSN("89-13-1234"))
  }

  it should "not work for full SSN with long first segment" in {
    SSN.fromString("8991-13-1234") shouldEqual Left(InvalidSSN("8991-13-1234"))
  }

  it should "not work for full SSN with 000 first segment" in {
    SSN.fromString("000-13-1234") shouldEqual Left(InvalidSSN("000-13-1234"))
  }

  it should "not work for full SSN with 666 first segment" in {
    SSN.fromString("666-13-1234") shouldEqual Left(InvalidSSN("666-13-1234"))
  }

  it should "not work for full SSN with 900 >= first segment #1" in {
    SSN.fromString("900-13-1234") shouldEqual Left(InvalidSSN("900-13-1234"))
  }

  it should "not work for full SSN with 900 >= first segment #2" in {
    SSN.fromString("901-13-1234") shouldEqual Left(InvalidSSN("901-13-1234"))
  }

  it should "not work for full SSN with short second segment" in {
    SSN.fromString("899-1-1234") shouldEqual Left(InvalidSSN("899-1-1234"))
  }

  it should "not work for full SSN with long second segment" in {
    SSN.fromString("899-131-1234") shouldEqual Left(InvalidSSN("899-131-1234"))
  }

  it should "not work for full SSN with 00 second segment" in {
    SSN.fromString("899-00-1234") shouldEqual Left(InvalidSSN("899-00-1234"))
  }

  it should "not work for full SSN with short third segment" in {
    SSN.fromString("899-13-12341") shouldEqual Left(InvalidSSN("899-13-12341"))
  }

  it should "not work for full SSN with long third segment" in {
    SSN.fromString("899-13-123") shouldEqual Left(InvalidSSN("899-13-123"))
  }

  it should "not work for full SSN with 0000 third segment" in {
    SSN.fromString("899-13-0000") shouldEqual Left(InvalidSSN("899-13-0000"))
  }

  it should "not work for long full SSN - no hyphens" in {
    SSN.fromString("8991312341") shouldEqual Left(InvalidSSN("8991312341"))
  }

  it should "not work for short full SSN - no hyphens" in {
    SSN.fromString("89913123") shouldEqual Left(InvalidSSN("89913123"))
  }

  it should "not work for last four SSN with 0000" in {
    SSN.fromString("0000") shouldEqual Left(InvalidSSN("0000"))
  }

  it should "not work for long last four SSN" in {
    SSN.fromString("12341") shouldEqual Left(InvalidSSN("12341"))
  }

  it should "not work for short last four SSN" in {
    SSN.fromString("123") shouldEqual Left(InvalidSSN("123"))
  }
}
