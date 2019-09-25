package org.byrde.service.response.exceptions

import org.byrde.service.response.{EmptyServiceResponse, Message, ServiceResponse, ServiceResponseType, Status}

import scala.util.control.NoStackTrace

abstract class ServiceResponseException[T <: ServiceResponseException[T]](_msg: String, _status: Status, _code: Int)
    extends Throwable(_msg)
    with EmptyServiceResponse[T] {
  self =>

  def apply(throwable: Throwable): T

  override def `type`: ServiceResponseType =
    ServiceResponseType.Error

  override def status: Status =
    _status

  override def code: Int =
    _code
}

object ServiceResponseException {
  case class TransientServiceResponseException(msg: String, override val status: Status, override val code: Int) extends ServiceResponseException[TransientServiceResponseException](msg, status, code) with NoStackTrace {
    override def apply(throwable: Throwable): TransientServiceResponseException =
      TransientServiceResponseException(throwable.getMessage, status, code)

    override def apply(_code: Int): TransientServiceResponseException =
      TransientServiceResponseException(msg, status, _code)
  }

  def apply(that: ServiceResponse[Message]): TransientServiceResponseException =
    TransientServiceResponseException(that.response.message, that.status, that.code)
}
