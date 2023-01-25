package org.byrde.commons

import org.byrde.commons.types.Money

import Money._
import org.scalatest.flatspec.AnyFlatSpec

class MoneySpec extends AnyFlatSpec {
  "Money" should "change the scale to the correct global one" in {
    val bigDecimal = BigDecimal(1.05321)
    val result = Money.scaled(bigDecimal)
    assert(result.toBigDecimal == 1.05)
    assert(result.toBigDecimal.scale == 2)
  }

  it should "not let you use the default apply method" in {
    assertDoesNotCompile("new Money(BigDecimal(1))")
  }

  it should "not let you create a Money with a scale greater than 2" in {
    val bigDecimal = BigDecimal(1).setScale(3)
    assert(Money(bigDecimal) == Left(InvalidMoney(bigDecimal)))
  }
}
