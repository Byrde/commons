package org.byrde.commons.utils.exception

import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0500
import org.byrde.commons.models.services.{DefaultServiceResponse, ServiceResponse}

import play.api.libs.json.Writes

case class ServiceResponseException(
  _msg: String,
  _code: Int,
  _status: Int) extends Throwable(_msg) with DefaultServiceResponse {
  override def msg: String =
    _msg

  override def status: Int =
    _status

  override def code: Int =
    _code
}

object ServiceResponseException {
  def apply[T](serviceResponse: ServiceResponse[T])(implicit writes: Writes[T]): ServiceResponseException =
    ServiceResponseException(
      serviceResponse.msg,
      serviceResponse.code,
      serviceResponse.status
    )

  def apply(ex: Throwable): ServiceResponseException =
    E0500.copy(_msg = ex.getMessage)
}