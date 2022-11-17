package org.byrde.support.types

import org.byrde.support.types.Phone.{Area, Country, Exchange, Ext}

object Phone {
  sealed trait PhoneValidationError

  case class PhoneEmpty(value: String) extends PhoneValidationError

  case class CountryNotFound(value: String) extends PhoneValidationError

  case class PhoneInvalid(value: String) extends PhoneValidationError

  private type Country = String

  private type Area = String

  private type Exchange = String

  private type Ext = String

  private val NorthAmericanNumber: String => Boolean =
    phone => phone.length == 11 && phone.startsWith("1")

  private val AustralianNumber: String => Boolean =
    phone => phone.length == 11 && phone.startsWith("61")

  def fromStringFullRange(phone: String, country: String): Either[PhoneValidationError, Phone] =
    fromString(phone).orElse(fromStringWithCountry(phone, country))

  def fromString(phone: String): Either[PhoneValidationError, Phone] =
    normalizePhone(phone) match {
      case phone if phone.isEmpty =>
        Left(PhoneEmpty(phone))

      case phone if NorthAmericanNumber(phone) =>
        toNorthAmericanPhone(phone)

      case phone if AustralianNumber(phone) =>
        toAustralianPhone(phone)

      case phone =>
        Left(PhoneInvalid(phone))
    }

  def fromStringWithCountry(phone: String, countryCode: String): Either[PhoneValidationError, Phone] =
    countryCode.toUpperCase match {
      case "CA" | "US" =>
        toNorthAmericanPhone(normalizePhone(phone))

      case "AU" =>
        toAustralianPhone(normalizePhone(phone))
    }

  private [support] def toNorthAmericanPhone(phone: String): Either[PhoneValidationError, Phone] = {
    def extractCountryCode(phone: String): String =
      phone.charAt(0).toString

    def extractAreaCode(phone: String): String =
      phone.substring(1, 4)

    def extractExchangeCode(phone: String): String =
      phone.substring(4, 7)

    def extractExtensionCode(phone: String): String =
      phone.substring(7, 11)

    def toPhone(phone: String): Phone =
      Phone(
        extractCountryCode(phone),
        extractAreaCode(phone),
        extractExchangeCode(phone),
        extractExtensionCode(phone)
      )

    phone match {
      case normalizedPhone if normalizedPhone.length == 11 =>
        Right(toPhone(normalizedPhone))

      case normalizedPhone if normalizedPhone.length == 10 =>
        Right(toPhone(s"1$normalizedPhone"))

      case phone =>
        Left(PhoneInvalid(phone))
    }
  }

  private [support] def toAustralianPhone(phone: String): Either[PhoneValidationError, Phone] = {
    def extractCountryCode(phone: String): String =
      phone.substring(0, 2)

    def extractAreaCode(phone: String): String =
      phone.charAt(2).toString

    def extractExchangeCode(phone: String): String =
      phone.substring(3, 7)

    def extractExtensionCode(phone: String): String =
      phone.substring(7, 11)

    def toPhone(phone: String): Phone =
      Phone(
        extractCountryCode(phone),
        extractAreaCode(phone),
        extractExchangeCode(phone),
        extractExtensionCode(phone)
      )

    phone match {
      case normalizedPhone if normalizedPhone.length == 11 =>
        Right(toPhone(normalizedPhone))

      case normalizedPhone if normalizedPhone.length == 10 =>
        Right(toPhone(s"61${normalizedPhone.drop(1)}"))

      case phone =>
        Left(PhoneInvalid(phone))
    }
  }

  private def normalizePhone(phone: String): String =
    phone.trim.replaceAll("[^\\d]", "")
}

case class Phone(country: Country, area: Area, exchange: Exchange, extension: Ext) {
  def unformatted: String =
    s"$country$area$exchange$extension"

  override def toString: String =
    s"+$country-$area-$exchange-$extension"
}
