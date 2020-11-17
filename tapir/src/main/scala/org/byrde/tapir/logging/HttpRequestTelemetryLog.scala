package org.byrde.tapir.logging

import akka.http.scaladsl.model.HttpRequest

import org.byrde.logging.Log
import org.byrde.tapir.support.RequestIdSupport

case class HttpRequestTelemetryLog(requestId: String, method: String, path: String, status: Int, duration: Long) extends Log {
  override def asMap: Map[String, String] =
    Map(
      "request_id" -> requestId,
      "request_method" -> method,
      "request_path" -> path,
      "request_status" -> status.toString,
      "request_duration" -> duration.toString
    )
}

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