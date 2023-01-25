package org.byrde.commons

//Requirements:
//1. Have a certain length (min 8, max 50)
//2. Have at least one symbol
//3. Have at least one digit
//4. Have at least 1 lower case and one upper case character
trait PasswordSupport {
  private def rule1(min: Int, max: Int): String => Boolean = s => s.length >= min && s.length <= max

  private def rule2(hasUpperLowerCase: Boolean): String => Boolean =
    s => !hasUpperLowerCase || !s.equals(s.toLowerCase) && !s.equals(s.toUpperCase)

  private def rule3(hasDigit: Boolean): String => Boolean =
    s => !hasDigit || s.codePoints.anyMatch(i => Character.isDigit(i))

  private def rule4(hasSymbol: Boolean): String => Boolean =
    s => !hasSymbol || s.codePoints.filter(i => !Character.isDigit(i)).anyMatch(i => !Character.isAlphabetic(i))

  @inline
  def isValidPassword(
    min: Int = 8,
    max: Int = 50,
    hasUpperLowerCase: Boolean = true,
    hasDigit: Boolean = true,
    hasSymbol: Boolean = true,
  ): String => Boolean =
    s =>
      rule1(min, max)(s) &&
        rule2(hasUpperLowerCase)(s) &&
        rule3(hasDigit)(s) &&
        rule4(hasSymbol)(s)
}

object PasswordSupport extends PasswordSupport
