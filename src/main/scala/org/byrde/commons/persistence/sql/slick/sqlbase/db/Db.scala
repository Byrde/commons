package org.byrde.commons.persistence.sql.slick.sqlbase.db

import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

abstract class Db[R <: Role](val jdbcConfiguration: DatabaseConfig[JdbcProfile]) extends Profile {
  import profile.api._

  private lazy val db: JdbcProfile#Backend#Database =
    jdbcConfiguration.db

  def run[A, E <: Effect](query: DBIOAction[A, NoStream, E])(implicit ev: R HasPrivilege E): Future[A] =
    db.run(query)
}
