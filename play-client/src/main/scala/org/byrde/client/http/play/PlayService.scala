package org.byrde.client.http.play

import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}

import org.byrde.client.http.play.conf.PlayConfig
import org.byrde.uri.Host

import zio.duration.Duration

class PlayService(val client: StandaloneWSClient)(val config: PlayConfig) extends StandaloneWSClient {
  
  def host: Host =
    config.host
  
  def clientId: Option[String] =
    config.clientId
  
  def clientToken: Option[String] =
    config.clientToken
  
  def callTimeout: Duration =
    config.callTimeout
  
  override def underlying[T]: T =
    client.underlying
  
  override def url(url: String): StandaloneWSRequest =
    client.url(url)
  
  override def close(): Unit =
    client.close()
  
}