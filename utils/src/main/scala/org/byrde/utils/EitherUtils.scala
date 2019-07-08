package org.byrde.utils

trait EitherUtils {
  implicit class Any2Right[T, TT](value: T) {
    @inline def success: Either[TT, T] = Right(value)
    @inline def r : Either[TT, T] = Right(value)
  }

  implicit class Any2Left[T, TT](value: T) {
    @inline def left: Either[T, TT] = Left(value)
    @inline def l : Either[T, TT] = Left(value)
  }
}

object EitherUtils extends EitherUtils