package org.byrde.commons.utils

import play.api.libs.json.{JsString, JsValue, Writes}

object WritesUtils {
  implicit val string: Writes[String] =
    new Writes[String] {
      override def writes(o: String): JsValue =
        JsString(o)
    }
}
