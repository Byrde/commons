package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.db.Db
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait BaseDataAccess[T <: BaseDataAccess[T]] {
  self: T =>
    def run[Result, R <: Role, E <: Effect](db: T => Db[R], query: T => DBIOAction[Result, NoStream, E])(implicit ev: R HasPrivilege E): Future[Result] =
      db(self).run(query(self))
}
