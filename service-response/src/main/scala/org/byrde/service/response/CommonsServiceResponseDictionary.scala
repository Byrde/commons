package org.byrde.service.response

import org.byrde.service.response.exceptions.{ClientException, ServerException}
import org.byrde.service.response.support.StatusSupport

// Commons codes are between 1 - 1000;
object CommonsServiceResponseDictionary extends StatusSupport {
  // OK
  object E0200 extends DefaultEmptyServiceResponse(200, 200)

  // Client errors (1 - 499)
  object E0400 extends ClientException("Bad Request", 400, 400)
  object E0401 extends ClientException("Unauthorized", 401, 401)
  object E0403 extends ClientException("Forbidden", 403, 403)
  object E0404 extends ClientException("Not Found", 404, 404)
  object E0405 extends ClientException("Method Not Allowed", 405, 405)
  object E0409 extends ClientException("Conflict", 409, 409)
  object E0415 extends ClientException("Unsupported Media Type", 415, 415)

  // Server errors (500 - 999)
  object E0500 extends ServerException("Internal Server Error", 500, 500)
  object E0502 extends ServerException("Bad Gateway", 500, 500)
  object E0504 extends ServerException("Service Timeout", 504, 504)
}
