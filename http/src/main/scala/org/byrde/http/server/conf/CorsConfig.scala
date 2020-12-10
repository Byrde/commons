package org.byrde.http.server.conf

import com.typesafe.config.Config

trait CorsConfig {
  def origins: Seq[String]
  
  def methods: Seq[String]
  
  def headers: Seq[String]
}

object CorsConfig {
  def apply(config: Config): CorsConfig =
    apply("origins", "methods", "headers", config)

  def apply(
    origins: String,
    methods: String,
    headers: String,
    config: Config
  ): CorsConfig = {
    val _origins =
      config
        .getString(origins)
        .split(", ")
        .toIndexedSeq

    val _methods =
      config
        .getString(methods)
        .split(", ")
        .toIndexedSeq

    val _headers =
      config
        .getString(headers)
        .split(", ")
        .toIndexedSeq

    new CorsConfig {
      override def origins: Seq[String] = _origins
  
      override def methods: Seq[String] = _methods
  
      override def headers: Seq[String] = _headers
    }
  }
}