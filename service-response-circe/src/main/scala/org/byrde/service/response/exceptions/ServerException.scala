package org.byrde.service.response.exceptions

case class ServerException(msg: String, override val status: Int, override val code: Int) extends ServiceResponseException[ServerException](msg, status, code) {
  override def apply(throwable: Throwable): ServerException =
    ServerException(throwable.getMessage, status, code)

  override def apply(_code: Int): ServerException =
    ServerException(msg, status, _code)

  override def isServerError: Boolean =
    true
}
