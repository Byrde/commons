package org.byrde.service.response

import org.byrde.service.response.DefaultServiceResponse.Message

trait DefaultServiceResponse extends ServiceResponse[Message] {
  self =>
  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  override def response: Message =
    Message(message)

  def apply(_msg: String): DefaultServiceResponse =
    apply(_msg, self.code)

  def apply(_msg: String, _code: Int): DefaultServiceResponse =
    new DefaultServiceResponse {
      override def `type`: ServiceResponseType =
        self.`type`

      override def message: String =
        _msg

      override def code: Int =
        _code

      override def status: Int =
        self.status
    }
}

object DefaultServiceResponse {
  case class Message(message: String)
}
