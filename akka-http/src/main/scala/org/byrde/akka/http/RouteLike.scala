package org.byrde.akka.http

import akka.http.scaladsl.server.Route

trait RouteLike {
  def routes: Route
}
