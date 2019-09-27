package org.byrde.logging

import org.byrde.service.response.exceptions.{BoxedResponseException, BoxedServiceResponseException}

trait Logging {
  def debug[T](elem: T)(implicit loggingInformation: LoggingFormatter[T]): Unit

  def info[T](elem: T)(implicit loggingInformation: LoggingFormatter[T]): Unit

  def warning[T](elem: T)(implicit loggingInformation: LoggingFormatter[T]): Unit

  def error[T](elem: T)(implicit loggingInformation: LoggingFormatter[T]): Unit
}

object Logging {
  implicit object ExceptionLoggingFormat$ extends LoggingFormatter[Throwable] {
    override def format(elem: Throwable): String =
      elem match {
        case ex: BoxedServiceResponseException =>
          formatBoxedResponse(ex) +
            s"service_status=${ex.status.value} " +
            s"service_code=${ex.code} "

        case ex: BoxedResponseException =>
          formatBoxedResponse(ex)

        case _ =>
          ""
      }
  }

  private def formatBoxedResponse(ex: BoxedResponseException): String =
    s"service_host=${ex.protocol + ex.host + ex.port.fold("")(port => s":$port")} " +
      s"service_method=${ex.method} " +
      s"""service_path="${ex.path}" """
}
