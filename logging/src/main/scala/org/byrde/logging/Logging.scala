package org.byrde.logging
import play.api.libs.json.{JsObject, Json}

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
    override def format(elem: Throwable): JsObject = {
      def serializeException(ex: Throwable): JsObject = {
        def loop(throwable: Throwable): JsObject = {
          val causedBy =
            Option(throwable) match {
              case Some(cause) =>
                Json.obj("causedBy" -> loop(cause.getCause))
              case None =>
                Json.obj()
            }

          Json.obj(
            "class" -> ex.getClass.getName(),
            "message" -> ex.getMessage,
            "stackTrace" -> ex.getStackTrace.map(_.toString)
          ) ++ causedBy
        }

        Json.obj(
          "class" -> ex.getClass.getName(),
          "message" -> ex.getMessage,
          "stackTrace" -> ex.getStackTrace.map(_.toString)
        ) ++ loop(ex.getCause)
      }

      Json.obj(
        "message" -> elem.getMessage,
        "exception" -> serializeException(elem)
      )
    }
  }
}