package org.byrde.commons.types

sealed trait SSN

object SSN {
  sealed trait SSNValidationError

  case class InvalidSSN(value: String) extends SSNValidationError

  private val FullR = """^(\d{3})-(\d{2})-(\d{4})$""".r

  private val FullNoHyphensR = """^(\d{9})$""".r

  private val LastFourR = """^(\d{4})$""".r

  case class Full(part1: String, part2: String, part3: String) extends SSN {
    def lastFour: String = part3

    override def toString: String = s"$part1-$part2-$part3"
  }

  case class LastFour(part3: String) extends SSN {
    override def toString: String = part3
  }

  val fromString: String => Either[SSNValidationError, SSN] = {
    case FullNoHyphensR(value) =>
      fromString(s"${value.take(3)}-${value.substring(3, 5)}-${value.takeRight(4)}")

    case FullR(part1, part2, part3) if isPart1Valid(part1) && isPart2Valid(part2) && isPart3Valid(part3) =>
      Right(Full(part1, part2, part3))

    case LastFourR(part3) if isPart3Valid(part3) =>
      Right(LastFour(part3))

    case value =>
      Left(InvalidSSN(value))
  }

  private def isPart1Valid(value: String): Boolean =
    value.length == 3 && value.forall(_.isDigit) && value != "000" && value != "666" && (value.toInt < 900)

  private def isPart2Valid(value: String): Boolean = value.length == 2 && value.forall(_.isDigit) && value != "00"

  private def isPart3Valid(value: String): Boolean = value.length == 4 && value.forall(_.isDigit) && value != "0000"
}
