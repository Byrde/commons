package org.byrde.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object FutureUtils {
  implicit class FutureTry2Future[T](value: Future[Try[T]])(implicit ec: ExecutionContext) {
    @inline def flattenTry: Future[T] =
      value.flatMap {
        case Success(res) =>
          Future.successful(res)
        case Failure(ex) =>
          Future.failed(ex)
      }
  }
}
