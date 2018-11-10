package org.byrde.akka.http.conf

import play.api.Configuration

case class CORSConfig(origins: Seq[String], methods: Seq[String], headers: Seq[String])

object CORSConfig {
  def apply(config: Configuration): CORSConfig =
    apply("origins", "methods", "headers", config)

  def apply(_origins: String,
            _methods: String,
            _headers: String,
            config: Configuration): CORSConfig = {
    val origins =
      config
        .get[Seq[String]](_origins)

    val methods =
      config
        .get[Seq[String]](_methods)

    val headers =
      config
        .get[Seq[String]](_headers)

    CORSConfig(origins, methods, headers)
  }
}