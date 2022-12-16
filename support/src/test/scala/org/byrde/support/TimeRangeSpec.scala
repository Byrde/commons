package org.byrde.support

import org.byrde.support.types.TimeRange

import java.time.Instant
import java.time.Instant.{ parse => ts }

import scala.concurrent.duration._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TimeRangeSpec extends AnyFlatSpec with Matchers {
  "TimeRange.sliceBy" should "slice and snap to the closest quarter of an hour when given a small range" in {
    val range = TimeRange(ts("2019-04-01T08:05:00.000Z"), ts("2019-04-01T08:10:00.000Z"))
    val slices = range.sliceBy(15.minutes)
    slices shouldEqual List(TimeRange(ts("2019-04-01T08:00:00.000Z"), ts("2019-04-01T08:15:00.000Z")))
  }

  it should "work" in {
    val list = List(10L, 20L, 30L, 40L).map(Instant.ofEpochMilli)
    TimeRange(list(0), list(1)).hasOverlap(TimeRange(list(2), list(3))) shouldBe false // totally disjoint
    TimeRange(list(0), list(2)).hasOverlap(TimeRange(list(1), list(3))) shouldBe true //  overlapping
    TimeRange(list(0), list(3)).hasOverlap(TimeRange(list(1), list(2))) shouldBe true //  subset
    TimeRange(list(0), list(1)).hasOverlap(TimeRange(list(1), list(2))) shouldBe false // endpoint shared
  }

  it should "slice and snap to the closest half an hour when given a range" in {
    val range = TimeRange(ts("2019-04-01T00:00:00.000Z"), ts("2019-04-02T00:00:00.000Z"))
    val slices = range.sliceBy(30.minutes)
    slices should have length 48
  }

  it should "slice and snap to the closest minute" in {
    val range = TimeRange(ts("2019-04-01T08:59:14.500Z"), ts("2019-04-01T09:09:59.999Z"))
    val slices = range.sliceBy(1.minute)
    slices should have length 11
  }
}
