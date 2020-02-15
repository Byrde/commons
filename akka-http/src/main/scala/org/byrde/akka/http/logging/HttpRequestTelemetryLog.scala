package org.byrde.akka.http.logging

import akka.http.scaladsl.model.HttpRequest

import org.byrde.akka.http.support.RequestIdSupport

case class HttpRequestTelemetryLog(requestId: String, method: String, path: String, status: Int, duration: Long)

object HttpRequestTelemetryLog extends RequestIdSupport {
  def apply(request: HttpRequest, status: Int, duration: Long): HttpRequestTelemetryLog =
    HttpRequestTelemetryLog(
      request.requestId,
      request.method.toString,
      request.uri.path.toString,
      status,
      duration
    )
}