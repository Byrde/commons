package org.byrde.akka.http.logging
import org.byrde.logging.LoggingFormatter

import akka.http.scaladsl.model.HttpRequest

trait HttpErrorLogging extends HttpLogging {
  def error[T](request: HttpRequest, throwable: Throwable)(implicit loggingInformation: LoggingFormatter[(HttpRequest, Throwable)]): Unit
}
