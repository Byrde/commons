package org.byrde.http.server

import sttp.tapir.Endpoint

case class MaterializedRoute[I, E, O, -R](endpoint: Endpoint[I, E, O, R], route: akka.http.scaladsl.server.Route)