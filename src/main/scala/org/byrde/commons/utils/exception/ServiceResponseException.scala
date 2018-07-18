package org.byrde.commons.utils.exception

import org.byrde.commons.models.services.DefaultServiceResponse

abstract class ServiceResponseException[T <: ServiceResponseException[T]](_msg: String, _code: Int, _status: Int)
    extends Throwable(_msg)
    with DefaultServiceResponse {
  self =>

  def apply(throwable: Throwable): T

  override def msg: String =
    _msg

  override def status: Int =
    _status

  override def code: Int =
    _code
}
