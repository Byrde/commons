package org.byrde.http.server

import sttp.tapir.Endpoint

case class MaterializedEndpoint[A, I, E, O, -R](endpoint: Endpoint[A, I, E, O, R], route: akka.http.scaladsl.server.Route)