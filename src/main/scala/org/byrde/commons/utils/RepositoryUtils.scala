package org.byrde.commons.utils
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}
import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.db.Db

import scala.concurrent.Future

object RepositoryUtils {
  case class RepositoryHelper[R <: Role](db: Profile[R] with Db[R]) {
    import db.api._

    implicit class Query2Runnable[Result, E <: Effect](query: DBIOAction[Result, NoStream, E]) {
      def run(implicit ev: R HasPrivilege E): Future[Result] =
        db.run(query)
    }
  }
}
