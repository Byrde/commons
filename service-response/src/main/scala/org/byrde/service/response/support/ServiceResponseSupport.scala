package org.byrde.service.response.support

import org.byrde.service.response.{Message, ServiceResponse}
import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

trait ServiceResponseSupport extends StatusSupport {
  implicit class ServiceResponse2ServiceResponseException(value: ServiceResponse[Option[Message]]) {
    @inline def toException(msg: String): TransientServiceResponseException =
      TransientServiceResponseException(value.response.map(_.toString).getOrElse(msg), value.status, value.code)
  }
}

object ServiceResponseSupport extends ServiceResponseSupport
