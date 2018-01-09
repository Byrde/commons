package org.byrde.commons.persistence.sqlbase.db

import slick.jdbc.JdbcProfile

trait Db extends Profile {
  implicit val db: JdbcProfile#Backend#Database = dbConfig.db
}
