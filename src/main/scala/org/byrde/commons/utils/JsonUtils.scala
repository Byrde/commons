package org.byrde.commons.utils

import play.api.libs.json._

import scala.language.implicitConversions

object JsonUtils {
  implicit class JsObjectHelpers(val json: JsObject) extends AnyVal {
    @inline def +?(option: Option[(String, JsValue)]): JsObject =
      option.fold(json)(json + _)

    @inline def ++?(option: Option[JsObject]): JsObject =
      option.fold(json)(json ++ _)
  }

  object Reads {
    implicit def string(key: String): Reads[String] =
      new Reads[String] {
        override def reads(json: JsValue): JsResult[String] =
          (json \ key)
            .validate[String]
      }
  }

  object Writes {
    implicit def string(key: String): OWrites[String] =
      new OWrites[String] {
        override def writes(o: String): JsObject =
          Json.obj(key -> JsString(o))
      }
  }

  object Format {
    def string(key: String): Format[String] =
      OFormat(Reads.string(key), Writes.string(key))
  }
}
