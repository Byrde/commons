package org.byrde.slick.migrations.engine

import org.byrde.slick.migrations.engine.SqlMigration.MigrationException

import slick.jdbc.SimpleJdbcAction

/**
 * A [[Migration]] defined in terms of SQL commands.
 * This trait implements `apply` and instead defines an
 * abstract [[sql]] method.
 */
trait SqlMigration extends Migration {
  /**
   * The SQL statements to run
   */
  def sql: Seq[String]

  def apply() =
    SimpleJdbcAction { ctx =>
      for(str <- sql)
        try ctx.session.withPreparedStatement(str)(_.execute())
        catch {
          case e: java.sql.SQLException =>
            throw MigrationException(s"Could not execute sql: '$str'", e)
        }
    }
}

/**
 * Convenience factory for [[SqlMigration]]
 * @example {{{ SqlMigration("drop table t1", "update t2 set x=10 where y=20") }}}
 */
object SqlMigration {
  //TODO mechanism other than exceptions?
  case class MigrationException(message: String, cause: Throwable) extends RuntimeException(message, cause)

  def apply(sql: String*): SqlMigration = {
    def sql0 =
      sql

    new SqlMigration {
      override val sql: Seq[String] =
        sql0
    }
  }
}