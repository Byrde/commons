package org.byrde.commons.utils

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.collection.immutable

object AkkaStreamsUtils {
  implicit class Iterable2Source[T](iterable: Iterable[T]) {
    def toSource(implicit materializer: Materializer): Source[T, NotUsed] =
      Source(iterable.to[immutable.Iterable])
  }
}