package org.byrde.support.types

import org.byrde.support.ExpandSupport._

import java.time.Instant.ofEpochMilli
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.time.{Instant, LocalDateTime, ZoneOffset}

import scala.concurrent.duration.FiniteDuration
import scala.math.floor

case class TimeRange(start: Instant, end: Instant) extends Serializable {
  require(end isAfter start)

  def isBetween(instant: Instant): Boolean =
    (instant isAfter start) && (instant isBefore end)

  def hasOverlap(that: TimeRange): Boolean =
    (start isBefore that.end) && (that.start isBefore end)

  def sliceBy(interval: FiniteDuration): Seq[TimeRange] = {
    val ∆ = interval.toMillis
    require(∆ > 0, "Can't slice by a negative number!")

    (∆ * floor(start.toEpochMilli / ∆)).toLong
      .until(end.toEpochMilli)
      .by(∆)
      .map(ofEpochMilli)
      .map(tick => tick -> tick.plusMillis(∆))
      .map((TimeRange.apply _).tupled)
  }
}

object TimeRange {
  implicit def ordering: Ordering[TimeRange] =
    (x: TimeRange, y: TimeRange) =>
      if (x.end isAfter y.end)
        1
      else if (x.end isBefore y.end)
        -1
      else
        0

  def YearSlicedByMonth(now: LocalDateTime): List[TimeRange] = {
    val start: LocalDateTime =
      now.minusYears(1).plusMonths(1).`with`(lastDayOfMonth)

    val end: LocalDateTime =
      now.`with`(lastDayOfMonth)

    val expand: LocalDateTime => LocalDateTime=
      _.plusMonths(1).`with`(lastDayOfMonth)

    val until: LocalDateTime => Boolean =
      _.isAfter(end)

    start
      .expand(expand, until)
      .iterator
      .map { localDateTime =>
        val start =
          localDateTime.withDayOfMonth(1).toInstant(ZoneOffset.UTC)

        val end =
          localDateTime.`with`(lastDayOfMonth).toInstant(ZoneOffset.UTC)

        TimeRange(start, end)
      }
      .toList
  }
}
