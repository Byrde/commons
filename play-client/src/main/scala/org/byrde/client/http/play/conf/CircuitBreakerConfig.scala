package org.byrde.client.http.play.conf

import com.typesafe.config.Config

import zio.duration.Duration

import scala.concurrent.duration._

trait CircuitBreakerConfig {

  def maxFailures: Int

  def callTimeout: Duration

}

object CircuitBreakerConfig {

  object Default extends CircuitBreakerConfig {
    override def maxFailures: Int = 3

    override def callTimeout: Duration = Duration.Finite(3.seconds.toNanos)
  }

  def apply(config: Config): CircuitBreakerConfig =
    apply("max-failures", "call-timeout", "reset-timeout")(config)

  def apply(
    _maxFailures: String,
    _callTimeout: String,
    _resetTimeout: String,
  )(config: Config): CircuitBreakerConfig =
    new CircuitBreakerConfig {
      def maxFailures: Int = config.getInt(_maxFailures)

      def callTimeout: Duration = Duration.Finite(config.getLong(_callTimeout).millis.toNanos)
    }

}