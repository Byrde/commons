package org.byrde.support.types

import org.byrde.support.types
import org.byrde.support.types.Email.{ Domain, DomainSuffix, Recipient }

object Email {
  sealed trait EmailValidationError

  case class InvalidEmail(value: String) extends EmailValidationError

  private type Recipient = String

  private type Domain = String

  private type DomainSuffix = String

  private val EmailRegex =
    """(^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])(?:\.([a-zA-Z0-9-].{0,61}[a-zA-Z0-9]))$""".r

  val fromString: String => Either[EmailValidationError, Email] = {
    case EmailRegex(recipient, domain, domainSuffix) =>
      Right(types.Email(recipient.toLowerCase, domain.toLowerCase, domainSuffix.toLowerCase))

    case email =>
      Left(InvalidEmail(email))
  }
}

case class Email(recipient: Recipient, domain: Domain, domainSuffix: DomainSuffix) {
  override def toString: String = s"${recipient.toLowerCase}@${domain.toLowerCase}.${domainSuffix.toLowerCase}"
}
