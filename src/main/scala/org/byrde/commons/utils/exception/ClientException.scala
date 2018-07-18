package org.byrde.commons.utils.exception

case class ClientException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ClientException](_msg, _code, _status) with NoStackTrace {
  override def apply(message: String): ClientException =
    ClientException(message, _code, _status)

  override def apply(throwable: Throwable): ClientException =
    ClientException(throwable.getMessage, _code, _status)
}
