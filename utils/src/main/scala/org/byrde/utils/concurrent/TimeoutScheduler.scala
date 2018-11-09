package org.byrde.utils.concurrent

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

case class TimeoutScheduler()(implicit system: ActorSystem) {
  def apply[T](future: String, after: FiniteDuration)(block: => Future[T])(failure: => Future[T])(implicit ec: ExecutionContext): Future[T] =
    Future.firstCompletedOf(
      List(block,
           akka.pattern.after(after, using = system.scheduler)(failure)))
}
