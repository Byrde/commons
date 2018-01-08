package org.byrde.commons.models.services

import play.api.libs.json.{JsString, JsValue, Writes}

trait DefaultServiceResponse extends ServiceResponse[String] {
  override val writes: Writes[String] = new Writes[String] {
    override def writes(o: String): JsValue = JsString(o)
  }

  override val response: String =
    msg
}
