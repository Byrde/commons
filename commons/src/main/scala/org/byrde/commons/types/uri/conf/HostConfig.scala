package org.byrde.commons.types.uri.conf

import org.byrde.commons.types.uri.{ Host, Port, Protocol }

import com.typesafe.config.Config

import scala.util.Try

object HostConfig {
  def apply(config: Config): Host = apply("protocol", "host", "port")(config)

  def apply(
    _protocol: String,
    _host: String,
    _port: String,
  )(config: Config): Host = {
    val protocol =
      config.getString(_protocol) match {
        case "http" =>
          Protocol.http

        case "https" =>
          Protocol.https
      }

    val host = config.getString(_host)

    val port = Try(config.getInt(_port)).map(Port.apply).toOption

    Host(host, protocol, port)
  }
}
