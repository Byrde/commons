package org.byrde.commons.utils

import play.api.libs.json.{JsObject, JsValue}

object JsonUtils {
  implicit class JsObjectHelpers(val json: JsObject) extends AnyVal {
    def +?(option: Option[(String, JsValue)]): JsObject =
      option.fold(json)(json + _)

    def ++?(option: Option[JsObject]): JsObject =
      option.fold(json)(json ++ _)
  }
}
