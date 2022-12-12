package org.byrde.support

import scala.annotation.tailrec

trait CollectionsSupport {
  implicit class IterableOnceOps[A](val col: IterableOnce[A]) {

    /** Fold while element satisfies the predicate. Stops on first unsatisfied predicate.
      * @param predicate
      *   check on next element to see whether we should continue to iterate
      * @param initial
      *   initial element to start the accumulation on
      * @param accumulate
      *   accumulation function
      */
    def foldWhile[A1 >: A](
      predicate: A => Boolean,
      initial: A1,
    )(accumulate: (A1, A1) => A1): A1 = foldLeftWhile[A1](predicate, initial)(accumulate)

    /** Fold while element satisfies the predicate. Stops on first unsatisfied predicate.
      * @param predicate
      *   check on next element to see whether we should continue to iterate
      * @param initial
      *   initial element to start the accumulation on
      * @param accumulate
      *   accumulation function
      */
    def foldLeftWhile[B](
      predicate: A => Boolean,
      initial: B,
    )(accumulate: (B, A) => B): B = {
      @tailrec
      def loop(it: Iterator[A], prev: B): B =
        if (it.hasNext) {
          val next = it.next()
          if (predicate(next))
            loop(it, accumulate(prev, next))
          else
            accumulate(prev, next)
        } else prev

      loop(col.iterator, initial)
    }

    /** Fold while element satisfies the predicate. Stops on first unsatisfied predicate.
      * @param predicate
      *   check on the next applied element to see whether we should continue to iterate
      * @param initial
      *   initial element to start the accumulation on
      * @param accumulate
      *   accumulation function
      */
    def foldWhileApply[A1 >: A, B](
      fn: A => B,
    )(predicate: B => Boolean, initial: A1)(accumulate: (A1, B) => A1): A1 =
      foldLeftWhileApply[A1, B](fn)(predicate, initial)(accumulate)

    /** Fold while element satisfies the predicate. Stops on first unsatisfied predicate.
      * @param predicate
      *   check on the next applied element to see whether we should continue to iterate
      * @param initial
      *   initial element to start the accumulation on
      * @param accumulate
      *   accumulation function
      */
    def foldLeftWhileApply[B, C](
      fn: A => C,
    )(predicate: C => Boolean, initial: B)(accumulate: (B, C) => B): B = {
      @tailrec
      def loop(it: Iterator[A], prev: B): B =
        if (it.hasNext) {
          val next = fn(it.next())
          if (predicate(next))
            loop(it, accumulate(prev, next))
          else
            accumulate(prev, next)
        } else prev

      loop(col.iterator, initial)
    }
  }
}

object CollectionsSupport extends CollectionsSupport
