package org.byrde.http.server

import sttp.tapir.Endpoint

trait Route[I, E, O, -R] {
  def endpoint: Endpoint[I, E, O, R]
  
  def route: akka.http.scaladsl.server.Route
  
  def ~ (_route: org.byrde.http.server.Route[_, _, _, _]): Seq[org.byrde.http.server.Route[_, _, _, _]] =
    Seq(this, _route)
}

object Route {
  def apply[I, E, O, R](_endpoint: Endpoint[I, E, O, R], _route: akka.http.scaladsl.server.Route): Route[I, E, O, R] =
    new Route[I, E, O, R] {
      override def endpoint: Endpoint[I, E, O, R] = _endpoint
  
      override def route: akka.http.scaladsl.server.Route = _route
    }
}