package org.byrde.commons.models.services

import org.byrde.commons.utils.WritesUtils

import play.api.libs.json.Writes

trait DefaultServiceResponse extends ServiceResponse[String] {
  self =>
    override implicit def writes: Writes[String] =
      WritesUtils.string

    override def response: String =
      msg

    protected def apply(message: String): DefaultServiceResponse =
      new DefaultServiceResponse {
        override def msg: String =
          message

        override def code: Int =
          self.code

        override def status: Int =
          self.status
      }
}
