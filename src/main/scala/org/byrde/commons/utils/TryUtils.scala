package org.byrde.commons.utils

import scala.util.{Failure, Success, Try}

object TryUtils {
  implicit class Any2Success[T](value: T) {
    @inline def success: Try[T] = Success(value)
    @inline def + : Try[T] = Success(value)
  }

  implicit class Throwable2Failure(value: Throwable) {
    @inline def failure: Try[Throwable] = Failure(value)
    @inline def - : Try[Throwable] = Failure(value)
  }
}
