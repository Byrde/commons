package org.byrde.clients.ahc.conf

import org.byrde.clients.circuitbreaker.conf.CircuitBreakerConfig
import org.byrde.uri.Host
import org.byrde.uri.conf.HostConfig

import com.typesafe.config.Config

import scala.util.Try

case class ServiceConfig(host: Host, circuitBreakerConfig: CircuitBreakerConfig, clientId: Option[String], clientToken: Option[String])

object ServiceConfig {
  def apply(serviceConfiguration: Config): ServiceConfig =
    ServiceConfig(
      HostConfig(serviceConfiguration),
      CircuitBreakerConfig(serviceConfiguration.getConfig("circuit-breaker")),
      Try(serviceConfiguration.getString("client-id")).toOption,
      Try(serviceConfiguration.getString("client-token")).toOption
    )
}
