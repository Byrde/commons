package org.byrde.commons.services.circuitbreaker

import scala.concurrent.{ExecutionContext, Future}

trait CircuitBreakerLike {
  def withCircuitBreaker[T](body: => Future[T])(implicit ec: ExecutionContext): Future[T]
}
