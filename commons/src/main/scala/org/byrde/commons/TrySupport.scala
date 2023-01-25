package org.byrde.commons

import scala.util.{ Failure, Success, Try }

trait TrySupport {
  implicit class Any2Success[T](value: T) {
    @inline def success: Try[T] = Success(value)
    @inline def !+ : Try[T] = Success(value)
  }

  implicit class Throwable2Failure[T](value: Throwable) {
    @inline def failure: Try[T] = Failure(value)
    @inline def !- : Try[T] = Failure(value)
  }
}

object TrySupport extends TrySupport
