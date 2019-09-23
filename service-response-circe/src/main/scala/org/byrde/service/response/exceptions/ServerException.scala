package org.byrde.service.response.exceptions

case class ServerException(msg: String, override val code: Int, override val status: Int) extends ServiceResponseException[ServerException](msg, code, status) {
  override def apply(throwable: Throwable): ServerException =
    ServerException(throwable.getMessage, status, code)

  override def apply(_code: Int): ServerException =
    ServerException(msg, status, _code)

  override def isServerError: Boolean =
    true
}
