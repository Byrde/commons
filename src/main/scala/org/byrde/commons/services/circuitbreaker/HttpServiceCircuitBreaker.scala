package org.byrde.commons.services.circuitbreaker

import java.util.concurrent.atomic.AtomicLong

import org.byrde.commons.utils.circuitbreaker.conf.CircuitBreakerConfig

import akka.actor.Scheduler
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Try}

class HttpServiceCircuitBreaker(serviceName: String, scheduler: Scheduler, circuitBreakerConfig: CircuitBreakerConfig)(implicit ec: ExecutionContext) extends CircuitBreaker(ec, scheduler, circuitBreakerConfig.maxFailures, circuitBreakerConfig.callTimeout, circuitBreakerConfig.resetTimeout) with CircuitBreakerLike {
  private val closedTime = new AtomicLong(0)

  this.onOpen {
    closedTime.set(System.currentTimeMillis)
  }

  override def withCircuitBreaker[T](fn: => Future[T]): Future[T] =
    withCircuitBreaker(fn, defineFailureFn).transform(identity, {
      case ex: CircuitBreakerOpenException =>
        val elapsedTime =
          System.currentTimeMillis - closedTime.get

        val msg =
          s"$serviceName ${ex.getMessage} (${elapsedTime}ms)"

        new CircuitBreakerOpenException(ex.remainingDuration, msg)

      case ex: TimeoutException =>
        val callTimeout =
          circuitBreakerConfig.callTimeout

        val msg =
          s"$serviceName ${ex.getMessage} (${callTimeout.toMillis}ms)"

        new TimeoutException(msg) with NoStackTrace

      case throwable =>
        throwable
    })

  private def defineFailureFn[T]: Try[T] => Boolean = {
    case Failure(_) =>
      true

    case _ =>
      false
  }
}