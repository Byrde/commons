package org.byrde.commons.utils.circuitbreaker.conf

import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

case class CircuitBreakerConfig(maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration)

object JwtConfig {
  def apply(config: Configuration): CircuitBreakerConfig =
    apply("max-failures", "call-timeout", "reset-timeout", config)

  def apply(_maxFailures: String,
            _callTimeout: String,
            _resetTimeout: String,
            config: Configuration): CircuitBreakerConfig = {
    val maxFailures =
      config
        .get[Int](_maxFailures)

    val callTimeout =
      config
        .get[FiniteDuration](_callTimeout)

    val resetTimeout =
      config
        .get[FiniteDuration](_resetTimeout)

    CircuitBreakerConfig(maxFailures, callTimeout, resetTimeout)
  }
}