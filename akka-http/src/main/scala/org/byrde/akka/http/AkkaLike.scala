package org.byrde.akka.http
import akka.actor.ActorSystem

trait AkkaLike {
  def system: ActorSystem
}
