package org.byrde.clients.ahc

import scala.reflect.ClassTag

case class BoxedTypedServiceResponseException[T: ClassTag](protocol: String, host: String, port: Option[String], method: String, path: String)(exception: Throwable)
  extends Exception {
  type TypeSafeResponse = T
}
