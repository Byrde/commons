package org.byrde.support

trait OptionSupport {
  implicit class Any2Some[T](value: T) {
    @inline def ? : Some[T] = Some(value)
  }
}

object OptionSupport extends OptionSupport
