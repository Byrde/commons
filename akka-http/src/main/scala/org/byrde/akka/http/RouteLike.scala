package org.byrde.akka.http

import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}
import org.byrde.akka.http.support.RouteSupport

import akka.http.scaladsl.server.Route

import scala.language.higherKinds

trait RouteLike[RuntimeModulesExt[T] <: RuntimeModulesLike[T], ModulesExt <: ModulesProviderLike[RuntimeModulesExt]]
  extends RouteSupport[RuntimeModulesExt, ModulesExt] {
  def routes: Route
}
