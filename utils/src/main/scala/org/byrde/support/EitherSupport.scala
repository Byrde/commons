package org.byrde.support

trait EitherSupport {
  implicit class Any2Right[T, TT](value: T) {
    @inline def success: Either[TT, T] = Right(value)
    @inline def r : Either[TT, T] = Right(value)
  }

  implicit class Any2Left[T, TT](value: TT) {
    @inline def left: Either[TT, T] = Left(value)
    @inline def l : Either[TT, T] = Left(value)
  }

  implicit class Either2Zip[T, TT](value: Either[TT, T]) {
    @inline def zip[A](other: Either[TT, A]): Either[TT, (T, A)] =
      value.flatMap(right1 => other.map(right2 => right1 -> right2))
  }
}

object EitherSupport extends EitherSupport