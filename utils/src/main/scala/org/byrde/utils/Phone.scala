package org.byrde.utils

import java.util.Locale

import org.byrde.utils.Phone.{Area, Country, Exchange, Ext}

object Phone {
  sealed trait PhoneValidationError

  object PhoneEmpty extends PhoneValidationError

  object CountryNotFound extends PhoneValidationError

  object PhoneInvalid extends PhoneValidationError

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
        Left(PhoneEmpty)

      case phone if NorthAmericanNumber(phone) =>
        toNorthAmericanPhone(phone)

      case phone if AustralianNumber(phone) =>
        toAustralianPhone(phone)

      case _ =>
        Left(PhoneInvalid)
    }

  def fromStringWithCountry(phone: String, country: String): Either[PhoneValidationError, Phone] =
    CountryCodesUtils
      .findByCountry(country)
      .orElse(CountryCodesUtils.findByCountryCode(country))
      .map(fromStringWithLocale(normalizePhone(phone), _))
      .getOrElse(Left(CountryNotFound))

  def fromStringWithLocale(phone: String, locale: Locale): Either[PhoneValidationError, Phone] =
    locale match {
      case Locale.US | Locale.CANADA | Locale.CANADA_FRENCH =>
        toNorthAmericanPhone(normalizePhone(phone))

      case locale if locale.getCountry == "AU" =>
        toAustralianPhone(normalizePhone(phone))
    }

  private [utils] def toNorthAmericanPhone(phone: String): Either[PhoneValidationError, Phone] = {
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

      case _ =>
        Left(PhoneInvalid)
    }
  }

  private [utils] def toAustralianPhone(phone: String): Either[PhoneValidationError, Phone] = {
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

      case _ =>
        Left(PhoneInvalid)
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
