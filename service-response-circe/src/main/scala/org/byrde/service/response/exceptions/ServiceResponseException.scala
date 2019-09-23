package org.byrde.service.response.exceptions

import org.byrde.service.response.{EmptyServiceResponse, Message, ServiceResponse, ServiceResponseType}

import scala.util.control.NoStackTrace

abstract class ServiceResponseException[T <: ServiceResponseException[T]](_msg: String, _status: Int, _code: Int)
    extends Throwable(_msg)
    with EmptyServiceResponse[T] {
  self =>

  def apply(throwable: Throwable): T

  override def `type`: ServiceResponseType =
    ServiceResponseType.Error

  override def status: Int =
    _status

  override def code: Int =
    _code
}

object ServiceResponseException {
  case class TransientServiceResponseException(msg: String, override val status: Int, override val code: Int) extends ServiceResponseException[TransientServiceResponseException](msg, status, code) with NoStackTrace {
    override def apply(throwable: Throwable): TransientServiceResponseException =
      TransientServiceResponseException(throwable.getMessage, code, status)

    override def apply(_code: Int): TransientServiceResponseException =
      TransientServiceResponseException(msg, status, _code)
  }

  def apply(that: ServiceResponse[Message]): TransientServiceResponseException =
    TransientServiceResponseException(that.response.message, that.status, that.code)
}
