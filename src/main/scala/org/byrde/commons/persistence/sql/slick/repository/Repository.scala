package org.byrde.commons.persistence.sql.slick.repository
import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.db.Db
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import scala.concurrent.Future

trait Repository[R <: Role] {
  self: Profile[R] with Db[R] =>
    import profile.api._

    protected implicit val AsEvidence: Profile[R] =
      self

    implicit class Query2Runnable[Result, E <: Effect](query: DBIOAction[Result, NoStream, E]) {
      def run(implicit ev: R HasPrivilege E): Future[Result] =
        self.run(query)
    }
}
