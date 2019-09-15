package org.byrde.service.response

trait DefaultServiceResponse extends ServiceResponse[Message] {
  self =>
  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  def apply(_code: Int): DefaultServiceResponse =
    apply(_code, response)

  def apply(_response: String): DefaultServiceResponse =
    apply(Message(_response))

  def apply(_response: Message): DefaultServiceResponse =
    apply(_response)

  def apply(_code: Int, _response: String): DefaultServiceResponse =
    apply(_code, Message(_response))

  def apply(_code: Int, _response: Message): DefaultServiceResponse =
    new DefaultServiceResponse {
      override def code: Int =
        _code

      override def status: Int =
        self.status

      override def `type`: ServiceResponseType =
        self.`type`

      override def response: Message =
        _response
    }
}
