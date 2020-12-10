package org.byrde.http.server.logging

import akka.http.scaladsl.model.HttpRequest

import org.byrde.logging.Log
import org.byrde.http.server.support.RequestIdSupport

case class HttpRequestLog(requestId: String, method: String, path: String) extends Log {
  override def asMap: Map[String, String] =
    Map(
      "request_id" -> requestId,
      "request_method" -> method,
      "request_path" -> path
    )
}

object HttpRequestLog extends RequestIdSupport {
  def apply(request: HttpRequest): HttpRequestLog =
    HttpRequestLog(request.requestId, request.method.toString, request.uri.path.toString)
}