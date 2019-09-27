package org.byrde.akka.http.logging

import java.util.UUID

import org.byrde.akka.http.support.RequestResponseHandlingSupport.IdHeader
import org.byrde.logging.{Logging, LoggingFormatter}

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest

trait HttpLogging {
  val logger: LoggingAdapter

  def debug[T](elem: (HttpRequest, T))(implicit formatter: LoggingFormatter[(HttpRequest, T)]): Unit =
    logger.debug(formatter.format(elem))

  def info[T](elem: (HttpRequest, T))(implicit formatter: LoggingFormatter[(HttpRequest, T)]): Unit =
    logger.info(formatter.format(elem))

  def warning[T](elem: (HttpRequest, T))(implicit formatter: LoggingFormatter[(HttpRequest, T)]): Unit =
    logger.warning(formatter.format(elem))

  def error[T](elem: (HttpRequest, T))(implicit formatter: LoggingFormatter[(HttpRequest, T)]): Unit =
    logger.error(formatter.format(elem))
}

object HttpLogging {
  implicit object HttpRequestInformationLoggingFormat$ extends LoggingFormatter[HttpRequest] {
    override def format(elem: HttpRequest): String = {
      def requestId =
        elem
          .headers
          .find(_.name.equalsIgnoreCase(IdHeader.name))
          .map(_.value)
          .map(UUID.fromString)
          .getOrElse(UUID.randomUUID)
          .toString

      "level=error " +
        s"request_id=$requestId " +
        s"""path="${elem.uri.path.toString}" """
    }
  }

  implicit object ExceptionWithHttpRequestLoggingFormatter$ extends LoggingFormatter[(HttpRequest, Throwable)] {
    override def format(elem: (HttpRequest, Throwable)): String = {
      val (req, ex) =
        elem._1 -> elem._2

      HttpRequestInformationLoggingFormat$.format(req) + Logging.ExceptionLoggingFormat$.format(ex)
    }
  }
}
