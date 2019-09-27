package org.byrde.akka.http.logging.impl

import org.byrde.akka.http.logging.HttpRequestLogging
import org.byrde.logging.LoggingFormatter

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest

class RequestHttpLogger(val system: ActorSystem) extends HttpRequestLogging {
  val name: String =
    "request"

  override val logger: LoggingAdapter =
    Logging(system, name)

  def request(epoch: Long, status: String, request: HttpRequest)(implicit formatter: LoggingFormatter[HttpRequest]): Unit = {
    val extras =
      s"status=$status "+
      s"epoch=${epoch}ms "

    logger.info(formatter.format(request) + extras)
  }
}
