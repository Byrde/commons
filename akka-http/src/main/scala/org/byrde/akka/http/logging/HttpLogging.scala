package org.byrde.akka.http.logging

import org.byrde.akka.http.support.RequestResponseHandlingSupport.IdHeader
import org.byrde.logging.{JsonLoggingFormat, Logging}

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest

import io.circe.Json

trait HttpLogging {
  val logger: LoggingAdapter

  def debug[T](elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.debug(loggingInformation.format(elem).toString)

  def debug[T](msg: String, elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.debug(loggingInformation.format(msg, elem).toString)

  def info[T](elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.info(loggingInformation.format(elem).toString)

  def info[T](msg: String, elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.info(loggingInformation.format(msg, elem).toString)

  def warning[T](elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.warning(loggingInformation.format(elem).toString)

  def warning[T](msg: String, elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.warning(loggingInformation.format(msg, elem).toString)

  def error[T](elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.error(loggingInformation.format(elem).toString)

  def error[T](msg: String, elem: (HttpRequest, T))(implicit loggingInformation: JsonLoggingFormat[(HttpRequest, T)]): Unit =
    logger.error(loggingInformation.format(msg, elem).toString)
}

object HttpLogging {
  implicit object HttpRequestInformationJsonLogginFormat extends JsonLoggingFormat[HttpRequest] {
    override def format(elem: HttpRequest): Json =
      Json.obj(
        "id" -> {
          elem
            .headers
            .find(_.name().equalsIgnoreCase(IdHeader.name))
            .map(_.value())
            .fold(Json.fromString("None"))(Json.fromString)
        },
        "uri" -> Json.fromString(elem.uri.toString),
        "method" -> Json.fromString(elem.method.value.toString),
        "headers" -> Json.fromValues(elem.headers.map(header => s"${header.name}: ${header.value}").map(Json.fromString)),
        "cookies" -> Json.fromValues(elem.cookies.map(cookie => s"${cookie.name}: ${cookie.value}").map(Json.fromString))
      )
  }

  implicit object ExceptionWithHttpRequestJsonLoggingFormat extends JsonLoggingFormat[(HttpRequest, Throwable)] {
    override def format(elem: (HttpRequest, Throwable)): Json = {
      val (req, ex) =
        elem._1 -> elem._2

      Json.obj(
        "request" -> HttpRequestInformationJsonLogginFormat.format(req),
        "exception" ->  Logging.ExceptionJsonLoggingFormat.format(ex))
    }
  }
}