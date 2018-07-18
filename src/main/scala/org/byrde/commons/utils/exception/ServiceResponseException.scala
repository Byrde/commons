package org.byrde.commons.utils.exception

import org.byrde.commons.models.services.DefaultServiceResponse

import scala.util.control.NoStackTrace

class ServiceResponseException(_msg: String, _code: Int, _status: Int)
    extends Throwable(_msg)
    with DefaultServiceResponse with NoStackTrace {
  def apply(message: String): ServiceResponseException =
    new ServiceResponseException(message, _code, _status)

  def apply(throwable: Throwable): ServiceResponseException =
    new ServiceResponseException(throwable.getMessage, _code, _status)

  override def msg: String =
    _msg

  override def status: Int =
    _status

  override def code: Int =
    _code
}
