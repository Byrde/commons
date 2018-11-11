package org.byrde.akka.http.modules
import akka.actor.ActorSystem

trait AkkaLike {
  def system: ActorSystem
}
