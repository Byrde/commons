package org.byrde.akka.http.logging

import org.byrde.logging.JsonLoggingFormat

import akka.http.scaladsl.model.HttpRequest

trait HttpRequestLogging extends HttpLogging {
  def request(id: String, epoch: Long, status: String, req: HttpRequest)(implicit loggingInformation: JsonLoggingFormat[HttpRequest]): Unit
}