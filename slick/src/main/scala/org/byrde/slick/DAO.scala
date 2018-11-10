package org.byrde.slick

import org.byrde.slick.conf.Profile
import org.byrde.slick.db.Db

import scala.concurrent.Future

trait DAO[R <: Role] {
  self: Profile[R] with Db[R] =>
    import profile.api._

    protected implicit val AsEvidence: Profile[R] =
      self

    implicit class Query2Runnable[Result, E <: Effect](query: DBIOAction[Result, NoStream, E]) {
      def run(implicit ev: R HasPrivilege E): Future[Result] =
        self.run(query)
    }
}
