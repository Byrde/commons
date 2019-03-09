package org.byrde.service.response.exceptions

import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.{DefaultServiceResponse, ServiceResponse, ServiceResponseType}

import scala.util.control.NoStackTrace

abstract class ServiceResponseException[T <: ServiceResponseException[T]](_msg: String, _code: Int, _status: Int)
    extends Throwable(_msg)
    with DefaultServiceResponse {
  self =>

  def apply(throwable: Throwable): T

  override def `type`: ServiceResponseType =
    ServiceResponseType.Error

  override def message: String =
    _msg

  override def status: Int =
    _status

  override def code: Int =
    _code
}

object ServiceResponseException {
  case class TransientServiceResponseException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[TransientServiceResponseException](_msg, _code, _status) with NoStackTrace {
    override def apply(throwable: Throwable): TransientServiceResponseException =
      TransientServiceResponseException(throwable.getMessage, code, status)
  }

  def apply(serviceResponse: ServiceResponse[Message]): TransientServiceResponseException =
    TransientServiceResponseException(serviceResponse.message, serviceResponse.code, serviceResponse.status)
}
