package org.byrde.clients.ahc

import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

case class BoxedTypedServiceResponseException[T: ClassTag](protocol: String, host: String, port: Option[String], method: String, path: String)(val exception: Throwable)
  extends Exception(exception.getMessage) with NoStackTrace {
  type TypeSafeResponse = T
}
