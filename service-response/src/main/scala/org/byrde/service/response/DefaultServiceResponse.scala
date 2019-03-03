package org.byrde.service.response

import play.api.libs.json.{JsString, Json, OWrites}

import scala.language.implicitConversions

trait DefaultServiceResponse extends ServiceResponse[String] {
  self =>
    private implicit def string(key: String): OWrites[String] =
      (o: String) =>
        Json.obj(key -> JsString(o))

    override implicit def writes: OWrites[String] =
      ServiceResponse.message

    override def `type`: ServiceResponseType =
      ServiceResponseType.Success

    override def response: String =
      msg

    def apply(_msg: String): DefaultServiceResponse =
      apply(_msg, self.code)

    def apply(_msg: String, _code: Int): DefaultServiceResponse =
      new DefaultServiceResponse {
        override def `type`: ServiceResponseType =
          self.`type`

        override def msg: String =
          _msg

        override def code: Int =
          _code

        override def status: Int =
          self.status
      }
}
