package org.byrde.slick

import scala.annotation.nowarn
import scala.concurrent.Future

import slick.dbio.{ DBIOAction, Effect, NoStream }

trait SlickDb[R <: SlickRole] {
  self: SlickProfile[R] =>
  private val underlyingDb = config.jdbc.db

  def run[Result, E <: Effect](query: DBIOAction[Result, NoStream, E])(
    implicit @nowarn ev: R SlickHasPrivilege E,
  ): Future[Result] = underlyingDb.run(query)

  def shutdown: Future[Unit] = underlyingDb.shutdown
}
