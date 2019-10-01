package org.byrde.akka.http.logging.impl

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.logging.LoggingFormatter

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest

class ErrorHttpLogger(val system: ActorSystem) extends HttpErrorLogging {
  val name: String =
    "error"

  override val logger: LoggingAdapter =
    Logging(system, name)

  def error[T](request: HttpRequest, throwable: Throwable)(implicit formatter: LoggingFormatter[(HttpRequest, Throwable)]): Unit =
    logger.error(throwable, formatter.format(request -> throwable))
}
