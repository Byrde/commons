package org.byrde.akka.http

import org.byrde.akka.http.support.RouteSupport

import akka.http.scaladsl.server.Route

trait RouteLike extends RouteSupport {
  def route: Route
}
