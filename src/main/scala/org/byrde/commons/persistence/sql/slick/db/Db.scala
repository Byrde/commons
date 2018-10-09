package org.byrde.commons.persistence.sql.slick.db

import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class Db[R <: Role](profile: Profile[R]) {
  private val underlyingDb =
    profile.jdbc.db

  def run[Result, E <: Effect](query: Profile[R] => DBIOAction[Result, NoStream, E])(implicit ev: R HasPrivilege E): Future[Result] =
    underlyingDb.run(query(profile))
}