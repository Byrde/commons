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

    def apply(message: String): DefaultServiceResponse =
      new DefaultServiceResponse {
        override def `type`: ServiceResponseType =
          self.`type`

        override def msg: String =
          message

        override def code: Int =
          self.code

        override def status: Int =
          self.status
      }
}
