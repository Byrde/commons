package org.byrde.tapir

import akka.http.scaladsl.server.Route

import sttp.tapir.Endpoint

trait TapirRoute[I, E, O, -R] {
  def endpoint: Endpoint[I, E, O, R]
  
  def route: Route
}

object TapirRoute {
  def apply[I, E, O, R](_endpoint: Endpoint[I, E, O, R], _route: Route): TapirRoute[I, E, O, R] =
    new TapirRoute[I, E, O, R] {
      override def endpoint: Endpoint[I, E, O, R] = _endpoint
  
      override def route: Route = _route
    }
}