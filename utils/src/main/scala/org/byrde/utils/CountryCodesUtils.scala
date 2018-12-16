package org.byrde.utils
import java.util.Locale

object CountryCodesUtils {
  private val countries =
    Locale
      .getAvailableLocales
      .map { locale =>
        normalizeKey(locale.getDisplayCountry) -> locale
      }
      .toMap

  def findByCountry(country: String): Option[Locale] =
    countries.get(country)

  def findByCountryCode(countryCode: String) =
    new Locale(Locale.getDefault.getLanguage, countryCode)

  private def normalizeKey(value: String) =
    value.toLowerCase.trim
}
