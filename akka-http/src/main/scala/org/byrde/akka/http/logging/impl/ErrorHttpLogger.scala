package org.byrde.akka.http.logging.impl

import org.byrde.akka.http.logging.HttpErrorLogging
import org.byrde.logging.JsonLoggingFormat

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest

class ErrorHttpLogger(val system: ActorSystem) extends HttpErrorLogging {
  val name: String =
    "error"

  override val logger: LoggingAdapter =
    Logging(system, name)

  def error[T](elem: HttpRequest, throwable: Throwable)(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, Throwable)]): Unit =
    logger.error(loggingInformation.format(throwable.getMessage, elem -> throwable).toString)
}
