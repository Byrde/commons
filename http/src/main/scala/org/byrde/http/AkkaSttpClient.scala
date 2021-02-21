package org.byrde.http

import akka.actor.ActorSystem

import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.{Request, Response}

import scala.concurrent.Future

class AkkaSttpClient()(implicit system: ActorSystem) extends SttpClient {
  private lazy val client =
    AkkaHttpBackend.usingActorSystem(system)
  
  override def send(request: Request[Either[String, String], Any]): Future[Response[Either[String, String]]] =
    client.send(request)
}