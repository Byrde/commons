package org.byrde.service.response

import org.byrde.service.response.Status.fromInt

import scala.language.implicitConversions

trait StatusHelpers {
  implicit def int2Status(value: Int): Status =
    fromInt(value)
}
