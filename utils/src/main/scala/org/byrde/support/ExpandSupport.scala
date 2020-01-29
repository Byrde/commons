package org.byrde.support

import scala.annotation.tailrec

trait ExpandSupport {
  implicit class ExpandAny[A, B](val value: A) {
    final def expand(expandFn: A => A, untilFn: A => Boolean): IterableOnce[A] = {
      @tailrec
      def innerExpand(acc: Seq[A], expandedValue: A): Seq[A] =
        expandedValue match {
          case innerExpandedValue if untilFn(innerExpandedValue) =>
            acc

          case innerExpandedValue =>
            innerExpand(acc :+ innerExpandedValue, expandFn(innerExpandedValue))
        }

      if (untilFn(value))
        Seq(value)
      else
        innerExpand(Seq(value), expandFn(value))
    }

    final def expand(acc: B)(expandFn: A => A, untilFn: A => Boolean, accFn: (B, A) => B): B = {
      @tailrec
      def innerExpand(acc: B, expandedValue: A): B =
        expandedValue match {
          case innerExpandedValue if untilFn(innerExpandedValue) =>
            acc

          case innerExpandedValue =>
            innerExpand(accFn(acc, innerExpandedValue), expandFn(innerExpandedValue))
        }

      if (untilFn(value))
        accFn(acc, value)
      else
        innerExpand(accFn(acc, value), expandFn(value))
    }
  }
}

object ExpandSupport extends ExpandSupport