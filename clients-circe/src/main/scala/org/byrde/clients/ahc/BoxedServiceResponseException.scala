package org.byrde.clients.ahc

case class BoxedServiceResponseException(protocol: String, host: String, port: Option[String], method: String, path: String)(val exception: Exception)
  extends Exception
