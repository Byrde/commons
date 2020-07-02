package org.byrde.client.http.play.conf

import org.byrde.uri.Host
import org.byrde.uri.conf.HostConfig

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.util.Try

case class PlayConfig(
  host: Host,
  clientId: Option[String],
  clientToken: Option[String],
  callTimeout: Duration
)

object PlayConfig {

  def apply(config: Config): PlayConfig =
    PlayConfig(
      HostConfig(config),
      Try(config.getString("client-id")).toOption,
      Try(config.getString("client-token")).toOption,
      Duration.fromNanos(config.getDuration("call-timeout").toNanos)
    )

}
