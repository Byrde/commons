package org.byrde.support

import scala.math._

case class Coordinates(latitude: Double, longitude: Double) {
  def distance(that: Coordinates): Double = {
    val latDelta = toRadians(this.latitude - that.latitude)
    val lonDelta = toRadians(this.longitude - that.longitude)
    val sinLat = sin(latDelta / 2)
    val sinLng = sin(lonDelta / 2)
    val a = sinLat * sinLat + (cos(toRadians(this.latitude)) * cos(toRadians(that.latitude)) * sinLng * sinLng)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val earthRadiusKm = 6371
    earthRadiusKm * c
  }
}
