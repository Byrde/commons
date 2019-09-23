package org.byrde.service.response.utils

import org.byrde.service.response.{Message, ServiceResponse}
import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

object ServiceResponseUtils {
  implicit class ServiceResponse2ServiceResponseException(value: ServiceResponse[Option[Message]]) {
    @inline def toException(msg: String): TransientServiceResponseException =
      TransientServiceResponseException(value.response.map(_.toString).getOrElse(msg), value.status, value.code)
  }
}
