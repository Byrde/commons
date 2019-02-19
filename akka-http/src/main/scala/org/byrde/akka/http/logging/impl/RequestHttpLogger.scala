package org.byrde.akka.http.logging.impl

import org.byrde.akka.http.logging.HttpRequestLogging
import org.byrde.logging.JsonLoggingFormat

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest

import io.circe.Json

class RequestHttpLogger(val system: ActorSystem) extends HttpRequestLogging {
  val name: String =
    "request"

  override val logger: LoggingAdapter =
    Logging(system, name)

  def request(epoch: Long, status: String, request: HttpRequest)(implicit loggingInformation: JsonLoggingFormat[HttpRequest]): Unit = {
    val innerRequest =
      Json.obj(
        "status" -> Json.fromString(status),
        "epoch" -> Json.fromString(s"${epoch}ms")
      )

    val json =
      Json.obj(
        "request" -> innerRequest,
        "info" -> loggingInformation.format(request))

    logger.info(json.toString())
  }
}
