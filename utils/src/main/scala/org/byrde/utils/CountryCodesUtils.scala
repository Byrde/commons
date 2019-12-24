package org.byrde.utils

import java.util.Locale

object CountryCodesUtils {
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

  def findByCountry(country: String): Option[Locale] =
    localesByCountry.get(normalizeKey(country))

  def findByCountryCode(countryCode: String): Option[Locale] =
    localesByCountryCode.get(normalizeKey(countryCode))

  private def normalizeKey(value: String) =
    value.toLowerCase.trim
}
