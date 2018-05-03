package org.byrde.commons.utils.exception

import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0500
import org.byrde.commons.models.services.{DefaultServiceResponse, ServiceResponse}

case class ServiceResponseException(_msg: String, _code: Int, _status: Int)
    extends Throwable(_msg)
    with DefaultServiceResponse {
  override def apply(message: String): ServiceResponseException =
    new ServiceResponseException(message, _code, _status)

  def apply(throwable: Throwable): ServiceResponseException =
    apply(new Exception(throwable))

  def apply(exception: Exception): ServiceResponseException =
    new ServiceResponseException(exception.getMessage, _code, _status)

  override def msg: String =
    _msg

  override def status: Int =
    _status

  override def code: Int =
    _code
}

object ServiceResponseException {
  def apply(throwable: Throwable): ServiceResponseException =
    apply(new Exception(throwable))

  def apply(ex: Exception): ServiceResponseException =
    E0500.copy(_msg = ex.getMessage)
}
