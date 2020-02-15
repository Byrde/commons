package org.byrde.slick.db

import org.byrde.slick.{SlickHasPrivilege, SlickRole}
import org.byrde.slick.conf.SlickProfile

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait SlickDb[R <: SlickRole] {
  self: SlickProfile[R] =>
    private val underlyingDb =
      config.jdbc.db

    def run[Result, E <: Effect](query: DBIOAction[Result, NoStream, E])(implicit ev: R SlickHasPrivilege E): Future[Result] =
      underlyingDb.run(query)

    def shutdown: Future[Unit] =
      underlyingDb.shutdown
}