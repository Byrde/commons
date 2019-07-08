package org.byrde.utils

//Requirements:
//1. Have a certain length (min 8, max 50)
//2. Have at least one symbol
//3. Have at least one digit
//4. Have at least 1 lower case and one upper case character
object PasswordUtils {
  private val rule1: String => Boolean =
    s => s.length >= 8 && s.length <= 50

  private val rule2: String => Boolean =
    s => !s.equals(s.toLowerCase) && !s.equals(s.toUpperCase)

  private val rule3: String => Boolean =
    _.codePoints.anyMatch(i => Character.isDigit(i))

  private val rule4: String => Boolean =
    _.codePoints.filter(i => !Character.isDigit(i)).anyMatch(i => !Character.isAlphabetic(i))

  private val rules: String => Boolean =
    s => rule1(s) && rule2(s) && rule3(s) && rule4(s)

  @inline val isValid: String => Boolean =
    rules
}