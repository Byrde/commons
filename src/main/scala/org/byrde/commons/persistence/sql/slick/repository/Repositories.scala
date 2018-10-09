package org.byrde.commons.persistence.sql.slick.repository

import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}
import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.repository.Repositories.RunnableQuery

import scala.concurrent.Future

trait Repositories[R <: Role] {
  self: Profile[R] with Db[R] =>
    import profile.api._

    def query[Result, E <: Effect](query: self.type => DBIOAction[Result, NoStream, E])(implicit ev: R HasPrivilege E): RunnableQuery[Result] =
      new RunnableQuery[Result] {
        override def run: Future[Result] =
          self.run(query(self))
      }
}

object Repositories {
  trait RunnableQuery[Result] {
    def run: Future[Result]
  }
}