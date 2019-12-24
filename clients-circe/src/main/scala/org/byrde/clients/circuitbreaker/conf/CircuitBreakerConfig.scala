package org.byrde.clients.circuitbreaker.conf

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class CircuitBreakerConfig(maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration)

object CircuitBreakerConfig {
  private implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  def apply(config: Config): CircuitBreakerConfig =
    apply("max-failures", "call-timeout", "reset-timeout", config)

  def apply(_maxFailures: String,
            _callTimeout: String,
            _resetTimeout: String,
            config: Config): CircuitBreakerConfig = {
    val maxFailures =
      config.getInt(_maxFailures)

    val callTimeout =
      config.getDuration(_callTimeout)

    val resetTimeout =
      config.getDuration(_resetTimeout)

    CircuitBreakerConfig(maxFailures, callTimeout, resetTimeout)
  }
}