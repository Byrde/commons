package org.byrde.commons.types

import java.time.temporal.TemporalAmount
import java.time.{ Instant, LocalDate, ZoneId, ZoneOffset, ZonedDateTime }

/** The important thing to note with this class is that the time component is effectively ignored. This class will
  * automatically round the given [[java.time.ZonedDateTime]] to the time at the start of the given day factoring the
  * timezone. This allows for convenient computations, comparisons, etc. at the date resolution.
  *
  * In case of "overlapping" with the local time-line, such as at an autumn daylight savings cutover, where there are
  * two valid offsets for the local date-time, this class will automatically round to the later of the two selected
  * offsets. (https://stackoverflow.com/questions/29143910/java-8-date-time-get-start-of-day-from-zoneddatetime)
  */
//TODO: Extensively test
case class Date private (private val _underlying: ZonedDateTime) {
  val underlying: ZonedDateTime = _underlying.toLocalDate.atStartOfDay(_underlying.getZone).withLaterOffsetAtOverlap()

  def zoneId: ZoneId = underlying.getZone

  def day: Int = underlying.getDayOfMonth

  def month: Int = underlying.getMonthValue

  def year: Int = underlying.getYear

  lazy val toLocalDate: LocalDate = underlying.toLocalDate

  lazy val toInstant: Instant = underlying.toInstant

  lazy val getEpochSecond: Long = toInstant.getEpochSecond

  lazy val toEpochDay: Long = toLocalDate.toEpochDay

  lazy val isOnOrAfterToday: Boolean = this.isOnOrAfterDate(Date.now())

  lazy val isBeforeToday: Boolean = this.isBeforeDate(Date.now())

  def isOnOrAfterDate(value: Date): Boolean = this.isOnOrAfterDate(value.toLocalDate)

  def isBeforeDate(value: Date): Boolean = this.isBeforeDate(value.toLocalDate)

  def isOnOrAfterDate(value: LocalDate): Boolean = this.toLocalDate.compareTo(value) >= 0

  def isBeforeDate(value: LocalDate): Boolean = this.toLocalDate.compareTo(value) < 0

  def isOnOrAfterDate(value: Instant): Boolean = this.isOnOrAfterDate(Date(value).toLocalDate)

  def isBeforeDate(value: Instant): Boolean = this.isBeforeDate(Date(value).toLocalDate)

  def isBetweenDate(from: Date, to: Date): Boolean = this.isBetweenDate(from.toLocalDate, to.toLocalDate)

  def isBetweenDate(from: LocalDate, to: LocalDate): Boolean = this.isOnOrAfterDate(from) && this.isBeforeDate(to)

  def isBetweenDate(from: Instant, to: Instant): Boolean =
    this.isBetweenDate(Date(from).toLocalDate, Date(to).toLocalDate)

  def plus(value: TemporalAmount): Date = Date(_underlying.plus(value))

  def minus(value: TemporalAmount): Date = Date(_underlying.minus(value))

  /** The output will be in the ISO-8601 date format. <pre>uuuu-MM-dd</pre>.
    */
  def toDateString: String = toLocalDate.toString

  /** The output will be in the ISO-8601 date time format.
    */
  def toDateTimeString: String = toInstant.toString

  /** The output will be in the ISO-8601 date format. <pre>uuuu-MM-dd</pre>.
    */
  override def toString: String = toDateString
}

object Date {
  def now(): Date = apply(ZonedDateTime.now())

  def apply(value: Instant): Date = apply(value.atZone(ZoneOffset.UTC))

  def apply(year: Int, month: Int, day: Int, zoneId: ZoneId): Date =
    apply(ZonedDateTime.of(year, month, day, 0, 0, 0, 0, zoneId))

  def ofEpochSecond(value: Long): Date = apply(Instant.ofEpochSecond(value))
}
