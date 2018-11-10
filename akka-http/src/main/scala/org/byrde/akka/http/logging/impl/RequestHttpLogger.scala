package org.byrde.akka.http.logging.impl

import org.byrde.akka.http.logging.HttpRequestLogging
import org.byrde.logging.JsonLoggingFormat

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest

import play.api.libs.json.{JsString, Json}

class RequestHttpLogger(val system: ActorSystem) extends HttpRequestLogging {
  val name: String =
    "request"

  override val logger: LoggingAdapter =
    Logging(system, name)

  def request(id: String, epoch: Long, status: String, req: HttpRequest)(implicit loggingInformation: JsonLoggingFormat[HttpRequest]): Unit = {
    val innerRequest =
      Json.obj(
        "id" -> JsString(id),
        "status" -> JsString(status),
        "epoch" -> JsString(s"${epoch}ms")
      )

    logger.info((innerRequest ++ loggingInformation.format(req)).toString())
  }
}
