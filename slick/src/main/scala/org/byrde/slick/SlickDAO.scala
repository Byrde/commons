package org.byrde.slick

import scala.concurrent.Future

trait SlickDAO[R <: SlickRole] {
  self: SlickProfile[R] with SlickDb[R] =>
  import profile.api._

  implicit protected val AsEvidence: SlickProfile[R] = self

  implicit class Query2Runnable[Result, E <: Effect](query: DBIOAction[Result, NoStream, E]) {
    def run(implicit ev: R SlickHasPrivilege E): Future[Result] = self.run(query)
  }
}
