package org.byrde.commons.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object FutureUtils {
  implicit def FutureTryToFuture[T](value: Future[Try[T]])(implicit ec: ExecutionContext): Future[T] =
    value.flatMap {
      case Success(res) =>
        Future.successful(res)
      case Failure(ex) =>
        Future.failed(ex)
    }
}
