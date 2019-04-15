package org.byrde.clients.ahc

import scala.util.control.NoStackTrace

case class BoxedServiceResponseException(protocol: String, host: String, port: Option[String], method: String, path: String)(val exception: Exception)
  extends Exception with NoStackTrace
