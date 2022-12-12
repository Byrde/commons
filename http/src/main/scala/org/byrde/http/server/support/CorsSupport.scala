package org.byrde.http.server.support

import org.byrde.http.server.conf.CorsConfig

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.respondWithHeaders

trait CorsSupport {
  private def origins(config: CorsConfig): Seq[RawHeader] =
    config
      .origins
      .map {
        RawHeader("Access-Control-Allow-Origin", _)
      }

  private def allowedMethods(config: CorsConfig): Seq[RawHeader] =
    config
      .methods
      .map {
        RawHeader("Access-Control-Allow-Methods", _)
      }

  private def allowHeaders(config: CorsConfig): Seq[RawHeader] =
    config
      .headers
      .map {
        RawHeader("Access-Control-Allow-Headers", _)
      }

  private def exposeHeaders(config: CorsConfig): Seq[RawHeader] =
    config
      .headers
      .map {
        RawHeader("Access-Control-Expose-Headers", _)
      }

  def corsDirective(config: CorsConfig): Directive0 =
    respondWithHeaders(origins(config) ++ allowedMethods(config) ++ allowHeaders(config) ++ exposeHeaders(config))
}
