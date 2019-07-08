package org.byrde.utils

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

trait TypeUtils
  extends OptionUtils
    with EitherUtils
    with FutureUtils
    with TryUtils {
  implicit def anyToFuture[T](value: T): Future[T] =
    value.f

  implicit def anyToFutureOption[T](value: T): Future[Option[T]] =
    value.?.f

  implicit def anyToFutureTry[T](value: T): Future[Try[T]] =
    value.!+.f

  implicit def anyToFutureEither[T, TT](value: T): Future[Either[TT, T]] =
    value.r.f

  implicit def anyToOption[T](value: T): Option[T] =
    value.?

  implicit def anyToEither[T, TT](value: T): Either[TT, T] =
    value.r

  implicit def anyToTry[T](value: T): Try[T] =
    value.!+
}

object TypeUtils extends TypeUtils
