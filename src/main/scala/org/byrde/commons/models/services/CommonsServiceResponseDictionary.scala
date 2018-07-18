package org.byrde.commons.models.services

import org.byrde.commons.utils.exception.ServiceResponseException

import scala.util.control.NoStackTrace

// Commons codes are between 1 - 1000;
object CommonsServiceResponseDictionary {
  case class ClientException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ClientException](_msg, _code, _status) with NoStackTrace {
    override def apply(message: String): ClientException =
      ClientException(message, _code, _status)

    override def apply(throwable: Throwable): ClientException =
      ClientException(throwable.getMessage, _code, _status)
  }

  case class ServerException(_msg: String, _code: Int, _status: Int) extends ServiceResponseException[ServerException](_msg, _code, _status) {
    override def apply(message: String): ServerException =
      ServerException(message, _code, _status)

    override def apply(throwable: Throwable): ServerException =
      ServerException(throwable.getMessage, _code, _status)
  }

  // OK
  object E0200 extends DefaultServiceResponse {
    override def msg: String =
      "Ok"

    override def status: Int =
      200

    override def code: Int   =
      200
  }
  // Client errors (1 - 499)
  object E0400 extends ClientException("Bad Request", 400, 400)
  object E0401 extends ClientException("Unauthorized", 401, 401)
  object E0403 extends ClientException("Forbidden", 403, 403)
  object E0404 extends ClientException("Not Found", 404, 404)
  object E0415 extends ClientException("Unsupported Media Type", 415, 415)
  // Server errors (500 - 999)
  object E0500 extends ServerException("Internal Server Error", 500, 500)
  object E0504 extends ServerException("Service Timeout", 504, 504)
}
