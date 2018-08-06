package org.byrde.commons.utils

import org.byrde.commons.models.services.ServiceResponse
import org.byrde.commons.utils.exception.ServiceResponseException.TransientServiceResponseException

object ServiceResponseUtils {
  implicit class ServiceResponse2ServiceResponseException(value: ServiceResponse[String]) {
    @inline def toException: TransientServiceResponseException =
      TransientServiceResponseException(value.msg, value.code, value.status)
  }
}
