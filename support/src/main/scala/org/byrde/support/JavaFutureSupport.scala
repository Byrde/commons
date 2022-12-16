package org.byrde.support

import java.util.concurrent.CompletableFuture

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.jdk.javaapi.FutureConverters

trait JavaFutureSupport {
  implicit class JavaFuture2ScalaFuture[T](future: java.util.concurrent.Future[T]) {
    def asScala(implicit ec: ExecutionContextExecutor): Future[T] =
      FutureConverters.asScala {
        CompletableFuture.supplyAsync(() => future.get, ec)
      }
  }
}

object JavaFutureSupport extends JavaFutureSupport
