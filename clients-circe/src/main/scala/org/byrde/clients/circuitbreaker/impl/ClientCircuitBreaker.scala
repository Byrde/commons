package org.byrde.clients.circuitbreaker.impl

import java.util.concurrent.atomic.AtomicLong

import org.byrde.clients.circuitbreaker.CircuitBreakerLike
import org.byrde.clients.circuitbreaker.conf.CircuitBreakerConfig

import akka.actor.Scheduler
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class ClientCircuitBreaker(serviceName: String, scheduler: Scheduler, circuitBreakerConfig: CircuitBreakerConfig)(implicit ec: ExecutionContext) extends CircuitBreaker(ec, scheduler, circuitBreakerConfig.maxFailures, circuitBreakerConfig.callTimeout, circuitBreakerConfig.resetTimeout) with CircuitBreakerLike {
  private val closedTime = new AtomicLong(0)

  this.onOpen {
    closedTime.set(System.currentTimeMillis)
  }

  override def withCircuitBreaker[T](fn: => Future[T]): Future[T] =
    withCircuitBreaker(fn, defineFailureFn)
      .recoverWith {
        case ex: CircuitBreakerOpenException =>
          val elapsedTime =
            System.currentTimeMillis - closedTime.get

          val msg =
            s"$serviceName ${ex.getMessage} (${elapsedTime}ms)"

          Future.failed(new CircuitBreakerOpenException(ex.remainingDuration, msg))

        case _ =>
          withCircuitBreaker(fn)
      }

  private def defineFailureFn[T]: Try[T] => Boolean = {
    case Failure(_) =>
      true

    case _ =>
      false
  }
}