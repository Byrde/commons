package org.byrde.service.response

import org.byrde.service.response.DefaultServiceResponse.Message

import io.circe.Encoder
import io.circe.generic.semiauto._

trait DefaultServiceResponse extends ServiceResponse[Message] {
  self =>
  override implicit def encoder: Encoder[Message] =
    deriveEncoder[Message]

  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  override def response: Message =
    Message(msg)

  def apply(_msg: String): DefaultServiceResponse =
    apply(_msg, self.code)

  def apply(_msg: String, _code: Int): DefaultServiceResponse =
    new DefaultServiceResponse {
      override implicit def encoder: Encoder[Message] =
        self.encoder

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

object DefaultServiceResponse {

  case class Message(message: String)

}
