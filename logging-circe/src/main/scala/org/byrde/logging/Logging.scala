package org.byrde.logging

import io.circe.Json

trait Logging {
  def debug[T](elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def debug[T](msg: String, elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def info[T](elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def info[T](msg: String, elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def warning[T](elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def warning[T](msg: String, elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def error[T](elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit

  def error[T](msg: String, elem: T)(implicit loggingInformation: JsonLoggingFormat[T]): Unit
}

object Logging {
  implicit object ExceptionJsonLoggingFormat extends JsonLoggingFormat[Throwable] {
    override def format(elem: Throwable): Json = {
      def serializeException(ex: Throwable): Json = {
        def loop(throwable: Throwable): Json = {
          val causedBy =
            Option(throwable) match {
              case Some(cause) =>
                Json.obj("causedBy" -> loop(cause.getCause))
              case None =>
                Json.obj()
            }

          Json.obj(
            "class" -> Json.fromString(ex.getClass.getName),
            "message" -> Json.fromString(ex.getMessage),
            "stackTrace" -> Json.fromValues(ex.getStackTrace.map(_.toString).map(Json.fromString)),
            "causedBy" -> causedBy)
        }

        Json.obj(
          "class" -> Json.fromString(ex.getClass.getName),
          "message" -> Json.fromString(ex.getMessage),
          "stackTrace" -> Json.fromValues(ex.getStackTrace.map(_.toString).map(Json.fromString)),
          "causedBy" -> loop(ex.getCause))
      }

      Json.obj(
        "message" -> Json.fromString(elem.getMessage),
        "exception" -> serializeException(elem))
    }
  }
}