package org.byrde.service.response.exceptions

case class ServerException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ServerException](_msg, _code, _status) {
  override def apply(message: String): ServerException =
    ServerException(message, _code, _status)

  override def apply(message: String, code: Int): ServerException =
    ServerException(message, code, _status)

  override def apply(throwable: Throwable): ServerException =
    ServerException(throwable.getMessage, _code, _status)

  override def isServerError: Boolean =
    true
}
