package org.byrde.commons.persistence.sql.slick.repository

import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait BaseRepositories[T <: BaseRepositories[T]] {
  self: T =>
    def run[Result, R <: Role, E <: Effect](query: T => DBIOAction[Result, NoStream, E])(db: T => Db[R])(implicit ev: R HasPrivilege E): Future[Result] =
      db(self).run(query(self))
}
