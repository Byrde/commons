package org.byrde.commons.persistence.sql.slick.db

import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait Db[R <: Role] {
  self: Profile[R] =>
    private val underlyingDb =
      configuration.jdbc.db

    def run[Result, E <: Effect](query: DBIOAction[Result, NoStream, E])(implicit ev: R HasPrivilege E): Future[Result] =
      underlyingDb.run(query)

    def shutdown: Future[Unit] =
      underlyingDb.shutdown
}