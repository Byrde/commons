package org.byrde.service.response.support

import org.byrde.service.response.Status
import org.byrde.service.response.Status.fromInt

trait StatusSupport {
  implicit def int2Status(value: Int): Status =
    fromInt(value)
}

object StatusSupport extends StatusSupport