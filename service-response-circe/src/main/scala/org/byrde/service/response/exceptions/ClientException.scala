package org.byrde.service.response.exceptions

import scala.util.control.NoStackTrace

case class ClientException(msg: String, override val code: Int, override val status: Int) extends ServiceResponseException[ClientException](msg, code, status) with NoStackTrace {
  override def apply(throwable: Throwable): ClientException =
    ClientException(throwable.getMessage, status, code)

  override def apply(_code: Int): ClientException =
    ClientException(msg, status, _code)

  override def isClientError: Boolean =
    true
}
