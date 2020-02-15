package org.byrde.service.response

trait EmptyServiceResponse[T <: EmptyServiceResponse[T]] extends ServiceResponse[Option[Message]] {
  self =>
  override def `type`: ServiceResponseType =
    ServiceResponseType.Success

  override def response: Option[Message] =
    Option.empty

  def apply(_code: Int): T
}
