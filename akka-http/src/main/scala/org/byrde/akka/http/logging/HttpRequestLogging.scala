package org.byrde.akka.http.logging

import org.byrde.logging.LoggingFormatter

import akka.http.scaladsl.model.HttpRequest

trait HttpRequestLogging extends HttpLogging {
  def request(epoch: Long, status: String, request: HttpRequest)(implicit loggingInformation: LoggingFormatter[HttpRequest]): Unit
}
