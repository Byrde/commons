package org.byrde.commons.utils

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.collection.immutable

object AkkaStreamsUtils {
  implicit class Iterable2Source[T](iterable: Iterable[T]) {
    @inline def toSource: Source[T, NotUsed] =
      Source(iterable.to[immutable.Iterable])
  }
}