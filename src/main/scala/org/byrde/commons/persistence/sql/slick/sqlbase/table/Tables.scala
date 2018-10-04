package org.byrde.commons.persistence.sql.slick.sqlbase.table

import org.byrde.commons.persistence.sql.slick.Role.Master
import org.byrde.commons.persistence.sql.slick.sqlbase.conf.Profile

abstract class Tables(val profile: Profile[Master]) {
	abstract class BaseTable[T](tag: profile.api.Tag, name: String) extends profile.api.Table[T](tag, name) {
    def id: profile.api.Rep[Long] =
      column[Long]("id", O.PrimaryKey, O.AutoInc)(profile.api.longColumnType)
  }
}