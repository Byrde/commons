package org.byrde.akka.http.logging

import akka.http.scaladsl.model.HttpRequest

import org.byrde.akka.http.support.RequestIdSupport
import org.byrde.logging.Log

case class HttpRequestLog(requestId: String, method: String, path: String) extends Log {
  override def asMap: Map[String, String] =
    Map(
      "request_id" -> requestId,
      "method" -> method,
      "path" -> path
    )
}

object HttpRequestLog extends RequestIdSupport {
  def apply(request: HttpRequest): HttpRequestLog =
    HttpRequestLog(request.requestId, request.method.toString, request.uri.path.toString)
}