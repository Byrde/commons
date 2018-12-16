package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.AhcExecutor
import org.byrde.clients.circuitbreaker.CircuitBreakerLike
import org.byrde.clients.circuitbreaker.impl.ClientCircuitBreaker

import akka.actor.ActorSystem

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseAhcExecutor extends AhcExecutor {
  self =>
  val circuitBreaker: CircuitBreakerLike =
    new ClientCircuitBreaker(
      name,
      system.scheduler,
      config.circuitBreakerConfig
    )(org.byrde.utils.ThreadPools.Trampoline)

  implicit def ec: ExecutionContext

  def system: ActorSystem

  override def executeRequest(request: StandaloneWSRequest): Future[StandaloneWSResponse] =
    circuitBreaker.withCircuitBreaker(request.execute().map(identity)(ec))
}


