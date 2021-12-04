package org.byrde.support

import java.util.concurrent.CompletableFuture

import scala.concurrent.Future
import scala.jdk.javaapi.FutureConverters
import scala.util.Try

trait JavaFutureSupport {
  implicit class JavaFuture2ScalaFuture[T](future: java.util.concurrent.Future[T]) {
    def asScala: Future[T] =
      Try(FutureConverters.asScala(CompletableFuture.supplyAsync(() => future.get))).fold(Future.failed, identity)
  }
}

object JavaFutureSupport extends JavaFutureSupport