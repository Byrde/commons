package org.byrde.commons.utils.concurrent

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

case class TimeoutScheduler(implicit actorSystem: ActorSystem) {
	def apply[T](future: String, after: FiniteDuration)(block: => Future[T])(failure: => Future[T])(ec: ExecutionContext): Future[T] =
		Future.firstCompletedOf(List(block, akka.pattern.after(after, using = actorSystem.scheduler)(failure)))
}
