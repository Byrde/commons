package org.byrde.utils

import java.util.Locale

import org.byrde.utils.Phone.PhoneInvalid
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PhoneTest extends AnyFlatSpec with Matchers {
  "Phone.fromString" should "extract North American phone parts" in {
    val phone = Phone.fromString("16138044534").toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "allow for roughly formatted North American phone - spaces" in {
    val phone = Phone.fromString("1 613 804 4534").toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "allow for roughly North American formatted phone - hyphens" in {
    val phone = Phone.fromString("1-613-804-4534").toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "extract Australian phone parts" in {
    val phone = Phone.fromString("61451266907").toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }

  it should "allow for roughly formatted Australian phone - spaces" in {
    val phone = Phone.fromString("61 4 5126 6907").toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }

  it should "allow for roughly Australian formatted phone - hyphens" in {
    val phone = Phone.fromString("61-4-5126-6907").toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }

  it should "fail when country code isn't present" in {
    Phone.fromString("6138044534") shouldBe Left(PhoneInvalid)
  }

  it should "fail wrong number of digits - North American" in {
    Phone.fromString("161380445344") shouldBe Left(PhoneInvalid)
  }

  it should "fail wrong number of digits - Australian" in {
    Phone.fromString("614512669072") shouldBe Left(PhoneInvalid)
  }

  "Phone.fromStringWithCountry" should "work with North American phone - CA" in {
    val phone = Phone.fromStringWithCountry("16138044534", "CA").toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "work with Australian phone - AU" in {
    val phone = Phone.fromStringWithCountry("61451266907", "AU").toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }

  it should "work with short form North American phone" in {
    val phone = Phone.fromStringWithCountry("6138044534", "CA").toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "work with short form Australian phone" in {
    val phone = Phone.fromStringWithCountry("0451266907", "AU").toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }

  "Phone.fromStringWithLocale" should "work with North American phone" in {
    val phone = Phone.fromStringWithLocale("16138044534", Locale.US).toOption.get
    phone.country shouldBe "1"
    phone.area shouldBe "613"
    phone.exchange shouldBe "804"
    phone.extension shouldBe "4534"
  }

  it should "work with Australian phone" in {
    val phone = Phone.fromStringWithLocale("61451266907", CountryCodesUtils.findByCountryCode("AU").get).toOption.get
    phone.country shouldBe "61"
    phone.area shouldBe "4"
    phone.exchange shouldBe "5126"
    phone.extension shouldBe "6907"
  }
}
