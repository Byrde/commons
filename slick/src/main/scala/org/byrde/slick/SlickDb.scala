package org.byrde.slick

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

trait SlickDb[R <: SlickRole] {
  self: SlickProfile[R] =>
    private val underlyingDb =
      config.jdbc.db
  
    def run[Result, E <: Effect](query: DBIOAction[Result, NoStream, E])(implicit @unchecked ev: R SlickHasPrivilege E): Future[Result] =
      underlyingDb.run(query)

    def shutdown: Future[Unit] =
      underlyingDb.shutdown
}
