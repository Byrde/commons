package org.byrde.service.response

import org.byrde.service.response.Status.fromInt

trait StatusHelpers {
  implicit def int2Status(value: Int): Status =
    fromInt(value)
}
