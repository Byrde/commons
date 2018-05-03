package org.byrde.commons.models.utils

object OptionUtils {
  implicit class Any2Some[T](value: T) {
    @inline def ? : Some[T] = Some(value)
  }
}
