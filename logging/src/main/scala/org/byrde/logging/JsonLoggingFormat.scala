package org.byrde.logging
import play.api.libs.json.{JsObject, JsString}

trait JsonLoggingFormat[-T] {
  def format(elem: T): JsObject

  def format(msg: String, elem: T): JsObject =
    format(elem) + ("message" -> JsString(msg))

  def format(msg: JsObject, elem: T): JsObject =
    format(elem) + ("message" -> msg)
}