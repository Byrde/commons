package org.byrde.client.http.play.conf

import org.byrde.uri.Host
import org.byrde.uri.conf.HostConfig

import com.typesafe.config.Config

import scala.util.Try

trait HttpClientConfig {

  def host: Host

  def clientId: Option[String]

  def clientToken: Option[String]

}

object HttpClientConfig {

  def apply(config: Config): HttpClientConfig =
    new HttpClientConfig {
      override def host: Host =
        HostConfig(config)

      override def clientId: Option[String] =
        Try(config.getString("client-id")).toOption

      override def clientToken: Option[String] =
        Try(config.getString("client-token")).toOption
    }

}
