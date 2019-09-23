package org.byrde.service.response

class DefaultServiceResponse(override val status: Int, override val code: Int, override val response: Message) extends ServiceResponse[Message] {
  self =>
  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  def apply(_code: Int): DefaultServiceResponse =
    apply(_code, response)

  def apply(_response: String): DefaultServiceResponse =
    apply(Message(_response))

  def apply(_response: Message): DefaultServiceResponse =
    apply(code, _response)

  def apply(_code: Int, _response: String): DefaultServiceResponse =
    apply(_code, Message(_response))

  def apply(_code: Int, _response: Message): DefaultServiceResponse =
    new DefaultServiceResponse(status, _code, _response)
}
