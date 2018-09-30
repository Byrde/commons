package org.byrde.commons.models.services

import org.byrde.commons.utils.JsonUtils
import play.api.libs.json.OWrites

trait DefaultServiceResponse extends ServiceResponse[String] {
  self =>
    override implicit def writes: OWrites[String] =
      JsonUtils.Writes.string(ServiceResponse.message)

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
