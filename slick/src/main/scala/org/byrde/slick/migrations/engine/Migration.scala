package org.byrde.slick.migrations.engine

import slick.dbio._

/**
 * The base of the migration type hierarchy.
 * Produces a DBIO that runs the migration
 */
trait Migration {
  def apply(): DBIO[Unit]
}

object Migration {
  /**
    * A [[Migration]] that can be reversed; that is,
    * it can provide a corresponding `Migration` that
    * will undo whatever this migration will do.
    */
  trait ReversibleMigration extends Migration {
    def reverse: Migration
  }

  /**
    * Holds a sequence of [[Migration]]s and performs them one after the other.
    */
  case class MigrationSeq(migrations: Migration*) extends Migration {
    final def apply(): DBIOAction[Unit, NoStream, Effect.All] =
      DBIO.seq(migrations.map(_()): _*)
  }

  /**
    * Holds a sequence of [[ReversibleMigration]]s and performs them one after the other.
    */
  class ReversibleMigrationSeq(override val migrations: ReversibleMigration*) extends MigrationSeq(migrations: _*) with ReversibleMigration {
    /**
      * @return the reverse [[MigrationSeq]]: Each migration will be reversed, and
      *         they will be in the reverse order.
      */
    def reverse =
      MigrationSeq(migrations.reverse.map(_.reverse): _*)
  }

  /**
    * A typeclass to determine the best way to combine migrations,
    * either into a [[ReversibleMigrationSeq]] or just a [[MigrationSeq]].
    * Used when you call '&' on [[Migration]]s.
    * Note that the migrations will be flattened; you will not end up with
    * something like `MigrationSeq(MigrationSeq(MigrationSeq(migA, migB), migC), migD)`.
    */
  class CanConcatMigrations[-A, -B, +C](val f: (A, B) => C)

  class CanConcatMigrationsLow {
    implicit def default[A <: Migration, B <: Migration]: CanConcatMigrations[A, B, MigrationSeq] =
      new CanConcatMigrations({
        case (MigrationSeq(as @ _*), b) =>
          MigrationSeq(as :+ b: _*)

        case (a, b) =>
          MigrationSeq(a, b)
      })
  }

  object CanConcatMigrations extends CanConcatMigrationsLow {
    implicit def reversible[A <: ReversibleMigration, B <: ReversibleMigration]: CanConcatMigrations[A, B, ReversibleMigrationSeq] = new CanConcatMigrations({
      case (rms: ReversibleMigrationSeq, b) =>
        new ReversibleMigrationSeq(rms.migrations :+ b: _*)

      case (a, b) =>
        new ReversibleMigrationSeq(a, b)
    })
  }

  implicit class MigrationConcat[M <: Migration](m: M) {
    /**
     *
     * @usecase def &(n: ReversibleMigration): ReversibleMigrationSeq
     * Append a [[ReversibleMigration]] to form either a
     * [[ReversibleMigrationSeq]] if the left side of `&` is also a [[ReversibleMigration]];
     * or else a plain [[MigrationSeq]]
     * @example {{{ val combined = mig1 & mig2 & mig3 }}}
     *
     * @usecase def &(n: Migration): MigrationSeq
     * Append another [[Migration]] to form a [[MigrationSeq]]
     * @param n the [[Migration]] to append
     * @example {{{ val combined = mig1 & mig2 & mig3 }}}
     */
    def &[N <: Migration, O](n: N)(implicit ccm: CanConcatMigrations[M, N, O]): O =
      ccm.f(m, n)
  }

  def empty: Migration =
    new Migration {
      override def apply(): DBIOAction[Unit, NoStream, Effect] =
        DBIO.successful(())
    }
}

