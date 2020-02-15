package org.byrde.client.http.conf

import com.typesafe.config.Config

import scala.concurrent.duration._

trait CircuitBreakerConfig {

  def maxFailures: Int

  def callTimeout: FiniteDuration

  def resetTimeout: FiniteDuration

}

object CircuitBreakerConfig {

  object Default extends CircuitBreakerConfig {
    override def maxFailures: Int = 3

    override def callTimeout: FiniteDuration = 3.seconds

    override def resetTimeout: FiniteDuration = 3.seconds
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

      def callTimeout: FiniteDuration = config.getLong(_callTimeout).millis

      def resetTimeout: FiniteDuration = config.getLong(_resetTimeout).millis
    }

}