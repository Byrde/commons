package org.byrde.http.server.support

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.respondWithHeaders

import org.byrde.http.server.conf.CorsConfig

trait CorsSupport {
  def corsConfig: CorsConfig

  lazy val origins: Seq[RawHeader] =
    corsConfig.origins.map {
      RawHeader("Access-Control-Allow-Origin", _)
    }

  lazy val allowedMethods: Seq[RawHeader] =
    corsConfig.methods.map {
      RawHeader("Access-Control-Allow-Methods", _)
    }

  lazy val allowHeaders: Seq[RawHeader] =
    corsConfig.headers.map {
      RawHeader("Access-Control-Allow-Headers", _)
    }

  lazy val exposeHeaders: Seq[RawHeader] =
    corsConfig.headers.map {
      RawHeader("Access-Control-Expose-Headers", _)
    }

  def cors: Directive0 =
    respondWithHeaders(
      origins ++
        allowedMethods ++
        allowHeaders ++
        exposeHeaders
    )
}