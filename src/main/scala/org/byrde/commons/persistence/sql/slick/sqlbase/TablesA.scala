package org.byrde.commons.persistence.sql.slick.sqlbase

import org.byrde.commons.persistence.sql.slick.sqlbase.db.Profile

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

abstract class TablesA(val jdbcConfiguration: DatabaseConfig[JdbcProfile]) extends Profile {
  import profile.api._

	abstract class BaseTableA[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }
}