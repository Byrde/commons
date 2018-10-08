package org.byrde.commons.persistence.sql.slick.table

import org.byrde.commons.persistence.sql.slick.conf.Profile

import slick.lifted.{Rep, Tag}

abstract class BaseTables(val profile: Profile[_]) {
	abstract class BaseTable[T](tag: Tag, name: String) extends profile.api.Table[T](tag, name) {
    def id: Rep[Long] =
      column[Long]("id", O.PrimaryKey, O.AutoInc)(profile.api.longColumnType)
  }
}