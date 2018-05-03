package org.byrde.commons.models.services

import org.byrde.commons.models.utils.WritesUtils

import play.api.libs.json.{JsValue, Writes}

trait DefaultServiceResponse extends ServiceResponse[String] {
  self =>
    def apply(message: String): DefaultServiceResponse =
      new DefaultServiceResponse {
        override def msg: String =
          message

        override def code: Int =
          self.code

        override def status: Int =
          self.status
      }


    override implicit val writes: Writes[String] =
      WritesUtils.string

    override val response: String =
      msg
}

object DefaultServiceResponse {
  implicit val writes: Writes[DefaultServiceResponse] =
    new Writes[DefaultServiceResponse] {
      override def writes(o: DefaultServiceResponse): JsValue =
        o.toJson
    }
}
