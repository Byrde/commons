package org.byrde.utils

import org.byrde.utils.Email.{Recipient, Domain, DomainSuffix}

object Email {
  sealed trait EmailValidationError

  object EmailInvalid extends EmailValidationError

  private type Recipient = String

  private type Domain = String

  private type DomainSuffix = String

  private val EmailRegex =
    ("""(^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+)""" +
      """@([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9]))?(?:\.([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$)""").r

  val fromString: String => Either[EmailValidationError, Email] = {
    case EmailRegex(recipient, domain, domainSuffix) =>
      Right(Email(recipient, domain, domainSuffix))

    case _ =>
      Left(EmailInvalid)
  }
}

case class Email(recipient: Recipient, domain: Domain, domainSuffix: DomainSuffix) {
  override def toString: String =
    s"$recipient@$domain.$domainSuffix"
}
