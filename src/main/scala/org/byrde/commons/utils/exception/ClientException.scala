package org.byrde.commons.utils.exception

import org.byrde.commons.models.services.ServiceResponseType

import scala.util.control.NoStackTrace

case class ClientException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ClientException](_msg, _code, _status) with NoStackTrace {
  override def `type`: ServiceResponseType =
    ServiceResponseType.Error

  override def apply(message: String): ClientException =
    ClientException(message, _code, _status)

  override def apply(throwable: Throwable): ClientException =
    ClientException(throwable.getMessage, _code, _status)
}
