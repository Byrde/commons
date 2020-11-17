package org.byrde.tapir

import akka.http.scaladsl.server.Route

import sttp.tapir.Endpoint

trait TapirRoute {
  def endpoint: Endpoint[_, _, _, _]
  
  def route: Route
}

object TapirRoute {
  def apply(_endpoint: Endpoint[_, _, _, _], _route: Route): TapirRoute =
    new TapirRoute {
      override def endpoint: Endpoint[_, _, _, _] = _endpoint
  
      override def route: Route = _route
    }
}