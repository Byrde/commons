package org.byrde.clients.ahc

import scala.util.control.NoStackTrace

case class BoxedServiceResponseException(protocol: String, host: String, port: Option[String], method: String, path: String)(val exception: Throwable)
  extends Exception(exception.getMessage) with NoStackTrace
