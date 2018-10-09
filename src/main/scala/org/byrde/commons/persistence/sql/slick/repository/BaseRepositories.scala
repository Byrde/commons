package org.byrde.commons.persistence.sql.slick.repository

import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}
import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.repository.BaseRepositories.RunnableQuery

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait BaseRepositories[R <: Role] {
  self: Db[R] =>
    def query[Result, E <: Effect](query: self.type => DBIOAction[Result, NoStream, E])(implicit ev: R HasPrivilege E): RunnableQuery[Result] =
      new RunnableQuery[Result] {
        override def run: Future[Result] =
          self.run(query(self))
      }
}

object BaseRepositories {
  trait RunnableQuery[Result] {
    def run: Future[Result]
  }
}