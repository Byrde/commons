package org.byrde.support

case class Money private (private val underlyingValue: BigDecimal) extends AnyVal with Ordered[Money] {
  def units: Long = underlyingValue.longValue

  def fraction: Long = underlyingValue.remainder(BigDecimal(1)).longValue

  def abs: Money = new Money(underlyingValue.abs)

  def negative: Money = new Money(-underlyingValue.abs)

  def toBigDecimal: BigDecimal = underlyingValue

  // http://blog.vanillajava.blog/2011/08/double-your-money-again.html
  def toDouble: Double = underlyingValue.toDouble

  def max(that: Money): Money = if (compare(that) >= 0) this else that

  def min(that: Money): Money = if (compare(that) <= 0) this else that

  def * (that: Money): Money = Money.scaled(underlyingValue * that.underlyingValue)

  def + (that: Money): Money = Money.scaled(underlyingValue + that.underlyingValue)

  def - (that: Money): Money = Money.scaled(underlyingValue - that.underlyingValue)

  def / (that: Money): Money = Money.scaled(underlyingValue / that.underlyingValue)

  override def compare(another: Money): Int = underlyingValue.compare(another.underlyingValue)
}

object Money {
  sealed trait MoneyValidationError

  case class InvalidMoney(value: BigDecimal) extends MoneyValidationError

  private val BalanceScale = 2

  private val RoundingMode: BigDecimal.RoundingMode.Value = BigDecimal.RoundingMode.HALF_UP

  val Zero: Money = Money.scaled(0)

  implicit val numeric: Numeric[Money] =
    new Numeric[Money] {
      override def plus(x: Money, y: Money): Money = x + y

      override def minus(x: Money, y: Money): Money = x - y

      override def times(x: Money, y: Money): Money = x * y

      override def negate(x: Money): Money = Money.scaled(-x.underlyingValue)

      override def fromInt(x: Int): Money = Money.scaled(x)

      override def toInt(x: Money): Int = x.underlyingValue.toInt

      override def toLong(x: Money): Long = x.underlyingValue.toLong

      override def toFloat(x: Money): Float = x.underlyingValue.toFloat

      override def toDouble(x: Money): Double = x.underlyingValue.toDouble

      override def compare(x: Money, y: Money): Int = x.compare(y)

      override def parseString(str: String): Option[Money] = Money(str).toOption
    }

  def apply(value: Double): Either[MoneyValidationError, Money] = Money(BigDecimal(value))

  def apply(value: String): Either[MoneyValidationError, Money] = Money(BigDecimal(value))

  def apply(value: BigDecimal): Either[MoneyValidationError, Money] =
    Either.cond(
      value.scale <= BalanceScale,
      Money.scaled(value),
      InvalidMoney(value),
    )

  def scaled(value: BigDecimal): Money = new Money(value.setScale(BalanceScale, RoundingMode))
}
