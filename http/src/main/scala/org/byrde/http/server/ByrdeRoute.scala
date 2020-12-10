package org.byrde.http.server

import akka.http.scaladsl.server.Route

import sttp.tapir.Endpoint

trait ByrdeRoute[I, E, O, -R] {
  def endpoint: Endpoint[I, E, O, R]
  
  def route: Route
}

object ByrdeRoute {
  def apply[I, E, O, R](_endpoint: Endpoint[I, E, O, R], _route: Route): ByrdeRoute[I, E, O, R] =
    new ByrdeRoute[I, E, O, R] {
      override def endpoint: Endpoint[I, E, O, R] = _endpoint
  
      override def route: Route = _route
    }
}