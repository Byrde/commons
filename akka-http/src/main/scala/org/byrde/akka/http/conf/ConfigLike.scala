package org.byrde.akka.http.conf

import akka.util.Timeout

trait ConfigLike {
  def config: com.typesafe.config.Config

  def name: String

  def interface: String

  def port: Int

  def timeout: Timeout

  def cors: CORSConfig
}
