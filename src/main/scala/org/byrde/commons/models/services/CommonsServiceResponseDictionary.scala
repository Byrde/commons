package org.byrde.commons.models.services

import org.byrde.commons.utils.exception.ServiceResponseException

// Commons codes are between 1 - 1000;
object CommonsServiceResponseDictionary {
  // OK
  object E0200 extends DefaultServiceResponse {
    override def msg: String = "Ok"
    override def status: Int = 200
    override def code: Int   = 0
  }
  // Client errors (1 - 499)
  object E0400 extends ServiceResponseException("Bad request", 1, 400)
  object E0401 extends ServiceResponseException("Unauthorized", 2, 401)
  object E0403 extends ServiceResponseException("Forbidden", 3, 403)
  object E0404 extends ServiceResponseException("Not found", 4, 404)
  // Server errors (500 - 999)
  object E0500
      extends ServiceResponseException("Internal Server Error", 500, 500)
  object E0504 extends ServiceResponseException("Service timeout", 501, 504)
}
