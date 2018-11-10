package org.byrde.service.response.exceptions

import org.byrde.service.response.ServiceResponseType

import scala.util.control.NoStackTrace

case class ClientException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ClientException](_msg, _code, _status) with NoStackTrace {
  override def `type`: ServiceResponseType =
    ServiceResponseType.Error

  override def apply(message: String): ClientException =
    ClientException(message, _code, _status)

  override def apply(message: String, code: Int): ClientException =
    ClientException(message, code, _status)

  override def apply(throwable: Throwable): ClientException =
    ClientException(throwable.getMessage, _code, _status)
}
