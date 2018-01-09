package org.byrde.commons.persistence.table

import org.byrde.commons.persistence.sqlbase.db.Db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

abstract class TablesA(val dbConfig: DatabaseConfig[JdbcProfile]) extends Db {
  import profile.api._
	abstract class BaseTableA[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }
}
