package org.byrde.clients.circuitbreaker
import scala.concurrent.Future

trait CircuitBreakerLike {
  def withCircuitBreaker[T](fn: => Future[T]): Future[T]
}
