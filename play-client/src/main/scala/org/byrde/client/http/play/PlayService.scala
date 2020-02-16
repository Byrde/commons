package org.byrde.client.http.play

import akka.actor.ActorSystem
import akka.stream.Materializer

import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}

import org.byrde.client.http.play.conf.{CircuitBreakerConfig, HttpClientConfig}
import org.byrde.uri.Host

import com.typesafe.config.Config

import zio.duration.Duration

trait PlayService extends StandaloneWSClient with HttpClientConfig with CircuitBreakerConfig

object PlayService {

  def apply(config: Config)(implicit system: ActorSystem): PlayService =
    apply(config, StandaloneAhcWSClient()(Materializer(system)))

  def apply(config: Config, client: StandaloneWSClient): PlayService =
    apply(HttpClientConfig(config), CircuitBreakerConfig(config), client)

  def apply(config1: HttpClientConfig, config2: CircuitBreakerConfig, client: StandaloneWSClient): PlayService =
    new PlayService {
      override def maxFailures: Int =
        config2.maxFailures

      override def callTimeout: Duration =
        config2.callTimeout

      override def host: Host =
        config1.host

      override def clientId: Option[String] =
        config1.clientId

      override def clientToken: Option[String] =
        config1.clientToken

      override def underlying[T]: T =
        client.underlying

      override def url(url: String): StandaloneWSRequest =
        client.url(url)

      override def close(): Unit =
        client.close()
    }

}