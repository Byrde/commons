package org.byrde.support

import scala.concurrent.Future

trait EitherSupport {
  implicit class Any2Right[T, TT](value: T) {
    @inline def success: Either[TT, T] = Right(value)
    @inline def r: Either[TT, T] = Right(value)
  }

  implicit class Any2Left[T, TT](value: TT) {
    @inline def left: Either[TT, T] = Left(value)
    @inline def l: Either[TT, T] = Left(value)
  }

  implicit class Either2Zip[T, TT](value: Either[TT, T]) {
    @inline def zip[A](other: Either[TT, A]): Either[TT, (T, A)] =
      value.flatMap(right1 => other.map(right2 => right1 -> right2))
  }

  implicit class Either2Future[T, TT <: Throwable](value: Either[TT, T]) {
    @inline def toFuture: Future[T] = value.fold(Future.failed, Future.successful)
  }

  implicit class GetEither[T, TT <: Throwable](value: Either[TT, T]) {
    @inline def get: T = value.fold(ex => throw new Exception("Get on left!", ex), identity)
  }
}

object EitherSupport extends EitherSupport
