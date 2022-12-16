package org.byrde.support

import scala.math.Ordering.Double.TotalOrdering

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.Matcher
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CoordinatesSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  private val (zero, upperBound) = (0.0, 6371.0 * math.Pi)

  private val beWithinKmBounds: Matcher[Double] = not((be < zero)(TotalOrdering) or (be > upperBound) (TotalOrdering))

  private val CornwallAreaCoordinates: Gen[Coordinates] =
    for {
      lat <- Gen.chooseNum(45.011025, 45.083469)
      lon <- Gen.chooseNum(-74.772141, -74.708126)
    } yield Coordinates(lat, lon)

  private val CityLimits = Coordinates(45.062965, -74.765300)

  private val MyHouse = Coordinates(45.069483, -74.776060)

  private val ParentsHouse = Coordinates(45.054762, -74.789938)

  private val CornwallAreaDiameter: Double = 30.0

  "Coordinates.distance" should "be consistent" in forAll(
    CornwallAreaCoordinates,
    CornwallAreaCoordinates,
    minSuccessful(10000),
  ) { (a, b) =>
    a.distance(b) should beWithinKmBounds
    a.distance(a) shouldBe zero
    b.distance(b) shouldBe zero
    a.distance(b) shouldEqual b.distance(a)
  }

  it should "work in extreme cases" in {
    val northPole = Coordinates(90.0, 135.0)
    val southPole = Coordinates(-90.0, 135.0)
    northPole.distance(southPole) should beWithinKmBounds
  }

  it should "work in small areas" in forAll(CornwallAreaCoordinates, CornwallAreaCoordinates, minSuccessful(10000)) {
    (a, b) =>
      a.distance(b) should not(be < zero or be > CornwallAreaDiameter)
  }

  it should "be closer to my house than my parents house" in {
    CityLimits.distance(ParentsHouse) should be > CityLimits.distance(MyHouse)
  }
}
