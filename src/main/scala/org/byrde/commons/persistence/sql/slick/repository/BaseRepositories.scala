package org.byrde.commons.persistence.sql.slick.repository

import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.table.BaseTables
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future
import scala.language.reflectiveCalls

trait BaseRepositories[T <: BaseRepositories[T, Tables], Tables <: BaseTables] {
  self: T =>
    def tables: Tables

    def run[Result, R <: Role, E <: Effect](query: T => DBIOAction[Result, NoStream, E])(db: T => Db[R])(implicit ev: R HasPrivilege E): Future[Result] =
      db(self).run(query(self))
}
