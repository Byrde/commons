package org.byrde.commons.persistence.sql.slick.repository

import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.table.BaseTables
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait BaseRepositories[T <: BaseRepositories[T, Tables], Tables <: BaseTables] {
  self: T =>
    def tables: Tables

    def run[Result, R <: Role, E <: Effect](query: T => Profile[R] => DBIOAction[Result, NoStream, E])(fn: T => Db[R])(implicit ev: R HasPrivilege E): Future[Result] = {
      val db =
        fn(self)

      db.run(query(self)(db.profile))
    }
}
