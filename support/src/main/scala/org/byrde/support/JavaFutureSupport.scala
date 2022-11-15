package org.byrde.support

import java.util.concurrent.CompletableFuture

import scala.concurrent.Future
import scala.jdk.javaapi.FutureConverters

trait JavaFutureSupport {
  implicit class JavaFuture2ScalaFuture[T](future: java.util.concurrent.Future[T]) {
    def asScala: Future[T] =
      FutureConverters.asScala {
        CompletableFuture.supplyAsync(() => future.get, scala.concurrent.ExecutionContext.global)
      }
  }
}

object JavaFutureSupport extends JavaFutureSupport