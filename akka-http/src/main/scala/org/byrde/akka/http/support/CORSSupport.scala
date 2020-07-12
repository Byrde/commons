package org.byrde.akka.http.support

import org.byrde.akka.http.conf.CORSConfig

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.respondWithHeaders
import akka.http.scaladsl.server.Route

trait CORSSupport {

  def cors: CORSConfig

  lazy val origins: Seq[RawHeader] =
    cors.origins.map {
      RawHeader("Access-Control-Allow-Origin", _)
    }

  lazy val allowedMethods: Seq[RawHeader] =
    cors.methods.map {
      RawHeader("Access-Control-Allow-Methods", _)
    }

  lazy val allowHeaders: Seq[RawHeader] =
    cors.headers.map {
      RawHeader("Access-Control-Allow-Headers", _)
    }

  lazy val exposeHeaders: Seq[RawHeader] =
    cors.headers.map {
      RawHeader("Access-Control-Expose-Headers", _)
    }

  def cors(route: Route): Route =
    respondWithHeaders(
      origins ++
        allowedMethods ++
        allowHeaders ++
        exposeHeaders
    )(route)

}
