package org.byrde.akka.http.conf

import com.typesafe.config.Config

case class CORSConfig(origins: Seq[String], methods: Seq[String], headers: Seq[String])

object CORSConfig {

  def apply(config: Config): CORSConfig =
    apply("origins", "methods", "headers", config)

  def apply(
    _origins: String,
    _methods: String,
    _headers: String,
    config: Config
  ): CORSConfig = {
    val origins =
      config
        .getString(_origins)
        .split(", ")
        .toIndexedSeq

    val methods =
      config
        .getString(_methods)
        .split(", ")
        .toIndexedSeq

    val headers =
      config
        .getString(_headers)
        .split(", ")
        .toIndexedSeq

    CORSConfig(origins, methods, headers)
  }

}