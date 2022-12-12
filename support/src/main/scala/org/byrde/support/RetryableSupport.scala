package org.byrde.support

import scala.concurrent.{ ExecutionContext, Future }

trait RetryableSupport {
  def withRetry[T](fn: =>Future[T])(implicit ec: ExecutionContext): Future[T] = withMaxRetry(3)(fn)

  def withMaxRetry[T](maxRetries: Int)(fn: =>Future[T])(implicit ec: ExecutionContext): Future[T] = {
    def innerWithRetry(retries: Int): Future[T] =
      fn.recoverWith {
        case ex if retries <= maxRetries =>
          innerWithRetry(retries + 1)

        case ex =>
          Future.failed(ex)
      }

    innerWithRetry(0)
  }
}

object RetryableSupport extends RetryableSupport
