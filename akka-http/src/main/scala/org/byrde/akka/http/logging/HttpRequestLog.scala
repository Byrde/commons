package org.byrde.akka.http.logging

import akka.http.scaladsl.model.HttpRequest

import org.byrde.akka.http.support.RequestIdSupport

case class HttpRequestLog(requestId: String, method: String, path: String)

object HttpRequestLog extends RequestIdSupport {
  def apply(request: HttpRequest): HttpRequestLog =
    HttpRequestLog(request.requestId, request.method.toString, request.uri.path.toString)
}