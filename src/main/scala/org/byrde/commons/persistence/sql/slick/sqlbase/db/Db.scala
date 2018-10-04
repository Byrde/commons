package org.byrde.commons.persistence.sql.slick.sqlbase.db

import org.byrde.commons.persistence.sql.slick.sqlbase.conf.Profile
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import scala.concurrent.Future

case class Db[R <: Role](profile: Profile[R]) {
  private val underlyingDb =
    profile.jdbc.db

  def run[A, E <: profile.api.Effect](query: profile.api.DBIOAction[A, profile.api.NoStream, E])(implicit ev: R HasPrivilege E): Future[A] =
    underlyingDb.run(query)
}