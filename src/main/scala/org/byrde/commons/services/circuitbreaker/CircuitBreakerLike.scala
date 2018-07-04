package org.byrde.commons.services.circuitbreaker

import scala.concurrent.Future

trait CircuitBreakerLike {
  def withCircuitBreaker[T](fn: => Future[T]): Future[T]
}
