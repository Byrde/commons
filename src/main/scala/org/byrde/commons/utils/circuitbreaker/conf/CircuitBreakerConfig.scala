package org.byrde.commons.utils.circuitbreaker.conf

import scala.concurrent.duration.FiniteDuration

case class CircuitBreakerConfig(maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration)