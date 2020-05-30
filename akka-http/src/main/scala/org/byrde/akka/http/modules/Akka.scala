package org.byrde.akka.http.modules
import akka.actor.ActorSystem

trait Akka {
  
  def system: ActorSystem
  
}
