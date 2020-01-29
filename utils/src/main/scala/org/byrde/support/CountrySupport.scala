package org.byrde.support

import java.util.Locale

trait CountrySupport {
  private val localesByCountry =
    Locale
      .getAvailableLocales
      .map { locale =>
        normalizeKey(locale.getDisplayCountry) -> locale
      }
      .toMap

  private val localesByCountryCode =
    Locale
      .getAvailableLocales
      .map { locale =>
        normalizeKey(locale.getCountry) -> locale
      }
      .toMap

  def findByStringFullRange(country: String): Option[Locale] =
    findByCountry(country).orElse(findByCountryCode(country))

  def findByCountry(country: String): Option[Locale] =
    localesByCountry.get(normalizeKey(country))

  def findByCountryCode(countryCode: String): Option[Locale] =
    localesByCountryCode.get(normalizeKey(countryCode))

  private def normalizeKey(value: String) =
    value.toLowerCase.trim
}

object CountrySupport extends CountrySupport