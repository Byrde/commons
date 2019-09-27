package org.byrde.service.response.exceptions

import scala.util.control.NoStackTrace

class BoxedResponseException(val protocol: String, val host: String, val port: Option[String], val method: String, val path: String)(val exception: Throwable)
  extends Exception(exception.getMessage) with NoStackTrace
