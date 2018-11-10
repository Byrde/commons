package org.byrde.akka.http.support

import org.byrde.akka.http.conf.CORSConfig

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.respondWithHeaders
import akka.http.scaladsl.server.Route

trait CORSSupport {
  def CORSConfig: CORSConfig

  lazy val origins: Seq[RawHeader] =
    CORSConfig.origins.map {
      RawHeader("Access-Control-Allow-Origin", _)
    }

  lazy val allowedMethods: Seq[RawHeader] =
    CORSConfig.methods.map {
      RawHeader("Access-Control-Allow-Methods", _)
    }

  lazy val allowHeaders: Seq[RawHeader] =
    CORSConfig.headers.map {
      RawHeader("Access-Control-Allow-Headers", _)
    }

  lazy val exposeHeaders: Seq[RawHeader] =
    CORSConfig.headers.map {
      RawHeader("Access-Control-Expose-Headers", _)
    }

  def cors(route: Route): Route =
    respondWithHeaders(
      origins ++
        allowedMethods ++
        allowHeaders ++
        exposeHeaders: _*
    )(route)
}
