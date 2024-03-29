package org.byrde.commons

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait FutureSupport {
  implicit class FutureTry2Future[T](value: Future[Try[T]])(implicit ec: ExecutionContext) {
    @inline def toFuture: Future[T] =
      value.flatMap {
        case Success(res) =>
          Future.successful(res)

        case Failure(ex) =>
          Future.failed(ex)
      }
  }

  implicit class Any2Future[T](value: T) {
    @inline def f: Future[T] = Future.successful(value)
  }
}

object FutureSupport extends FutureSupport
