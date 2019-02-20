package org.byrde.service.response.utils

import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.ServiceResponse
import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

object ServiceResponseUtils {
  implicit class ServiceResponse2ServiceResponseException(value: ServiceResponse[Message]) {
    @inline def toException: TransientServiceResponseException =
      TransientServiceResponseException(value.msg, value.code, value.status)
  }
}
