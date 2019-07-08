package org.byrde.utils

trait OptionUtils {
  implicit class Any2Some[T](value: T) {
    @inline def ? : Some[T] = Some(value)
  }

  @inline def fromBoolean(boolean: Boolean): Option[Boolean] =
    if (boolean) Some(boolean) else Option.empty[Boolean]
}

object OptionUtils extends OptionUtils
