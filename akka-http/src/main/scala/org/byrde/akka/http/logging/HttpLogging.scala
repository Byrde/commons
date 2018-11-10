package org.byrde.akka.http.logging

import org.byrde.akka.http.support.RequestResponseHandlingSupport.IdHeader
import org.byrde.logging.{JsonLoggingFormat, Logging}

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest

import play.api.libs.json.{JsObject, Json}

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
    override def format(elem: HttpRequest): JsObject =
      Json.obj(
        "id" -> elem.header[IdHeader].fold("None")(header => header.id.toString),
        "uri" -> elem.uri.toString,
        "method" -> elem.method.value.toString,
        "headers" -> elem.headers.map(header => s"${header.name}: ${header.value}"),
        "cookies" -> elem.cookies.map(cookie => s"${cookie.name}: ${cookie.value}")
      )
  }

  implicit object ExceptionWithHttpRequestJsonLoggingFormat extends JsonLoggingFormat[(HttpRequest, Throwable)] {
    override def format(elem: (HttpRequest, Throwable)): JsObject = {
      val (req, ex) =
        elem._1 -> elem._2

      HttpRequestInformationJsonLogginFormat.format(req) ++
        Logging.ExceptionJsonLoggingFormat.format(ex)
    }
  }
}