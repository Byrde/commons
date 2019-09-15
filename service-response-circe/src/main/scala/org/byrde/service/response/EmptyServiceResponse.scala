package org.byrde.service.response

trait EmptyServiceResponse extends ServiceResponse[Option[Message]] {
  self =>
  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  override def response: Option[Message] =
    Option.empty

  def apply(_code: Int): EmptyServiceResponse =
    new EmptyServiceResponse {
      override def code: Int =
        _code

      override def status: Int =
        self.status
    }
}
