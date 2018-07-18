package org.byrde.commons.models.services

import org.byrde.commons.utils.exception.ServiceResponseException

// Commons codes are between 1 - 1000;
object CommonsServiceResponseDictionary {
  sealed trait ClientException

  sealed trait ServerException

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
  object E0400 extends ServiceResponseException("Bad Request", 400, 400) with ClientException
  object E0401 extends ServiceResponseException("Unauthorized", 401, 401) with ClientException
  object E0403 extends ServiceResponseException("Forbidden", 403, 403) with ClientException
  object E0404 extends ServiceResponseException("Not Found", 404, 404) with ClientException
  object E0415 extends ServiceResponseException("Unsupported Media Type", 415, 415) with ClientException
  // Server errors (500 - 999)
  object E0500
      extends ServiceResponseException("Internal Server Error", 500, 500) with ServerException
  object E0504 extends ServiceResponseException("Service timeout", 504, 504) with ServerException
}
