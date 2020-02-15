package org.byrde.service.response.exceptions

import org.byrde.service.response.Status

import scala.util.control.NoStackTrace

case class ClientException(msg: String, override val status: Status, override val code: Int) extends ServiceResponseException[ClientException](msg, status, code) with NoStackTrace {
  override def apply(throwable: Throwable): ClientException =
    ClientException(throwable.getMessage, status, code)

  override def apply(_code: Int): ClientException =
    ClientException(msg, status, _code)

  override def isClientError: Boolean =
    true
}
