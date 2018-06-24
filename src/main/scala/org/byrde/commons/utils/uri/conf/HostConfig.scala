package org.byrde.commons.utils.uri.conf

import org.byrde.commons.models.uri.{Host, Port, Protocol}

import play.api.Configuration

object HostConfig {
  def apply(config: Configuration): Host =
    apply("protocol", "host", "port", config)

  def apply(_protocol: String,
            _host: String,
            _port: String,
            config: Configuration): Host = {
    val protocol =
      config
        .get[String](_protocol) match {
          case "http" =>
            Protocol.http
          case "https" =>
            Protocol.https
        }

    val host =
      config
        .get[String](_host)

    val port =
      config
        .getOptional[Int](_port)
        .map(Port.apply)

    Host(protocol, host, port)
  }
}
