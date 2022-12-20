package org.byrde.support

import java.util.concurrent.CompletableFuture

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.jdk.javaapi.FutureConverters
import scala.util.Try

trait JavaFutureSupport extends FutureSupport {
  implicit class JavaFuture2ScalaFuture[T](future: =>java.util.concurrent.Future[T]) {
    def asScala(implicit ec: ExecutionContextExecutor): Future[T] =
      FutureConverters
        .asScala {
          CompletableFuture.supplyAsync(() => Try(future.get), ec)
        }
        .toFuture
  }
}

object JavaFutureSupport extends JavaFutureSupport
