package org.byrde.clients.ahc.conf

import org.byrde.clients.ahc.conf.ServiceConfig.{ClientId, ClientToken}
import org.byrde.clients.circuitbreaker.conf.CircuitBreakerConfig
import org.byrde.uri.Host
import org.byrde.uri.conf.HostConfig

case class ServiceConfig(host: Host, circuitBreakerConfig: CircuitBreakerConfig, clientId: Option[ClientId], clientToken: Option[ClientToken])

object ServiceConfig {
  type ClientId = String

  type ClientToken = String

  def apply(serviceConfiguration: play.api.Configuration): ServiceConfig =
    ServiceConfig(
      HostConfig(serviceConfiguration),
      CircuitBreakerConfig(serviceConfiguration.get[play.api.Configuration]("circuit-breaker")),
      serviceConfiguration.getOptional[ClientId]("client-id"),
      serviceConfiguration.getOptional[ClientToken]("client-token")
    )
}