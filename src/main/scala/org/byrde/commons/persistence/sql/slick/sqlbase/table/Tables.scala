package org.byrde.commons.persistence.sql.slick.sqlbase.table

import org.byrde.commons.persistence.sql.slick.Role.Master
import org.byrde.commons.persistence.sql.slick.sqlbase.conf.Profile

abstract class Tables(val profile: Profile[Master]) {
  import profile.api._

	abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }
}