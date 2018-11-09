package org.byrde.utils

object OptionUtils {
  implicit class Any2Some[T](value: T) {
    @inline def ? : Some[T] = Some(value)
  }
}
