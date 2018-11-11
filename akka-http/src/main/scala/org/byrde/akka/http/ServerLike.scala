package org.byrde.akka.http

import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}
import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.{RequestResponseHandlingSupport, RouteSupport}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait ServerLike [
  RuntimeModulesExt[T] <: RuntimeModulesLike[T],
  ModulesExt <: ModulesProviderLike[RuntimeModulesExt],
  RuntimeModulesBuilderExt <: RuntimeModulesLike.RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt]
] extends RouteSupport with RequestResponseHandlingSupport {
  self =>

  type Domain = String

  trait RuntimeModulesDirectiveMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt, RuntimeModulesBuilderExt] {
    override lazy val provider: ModulesExt =
      self.provider

    override lazy val builder: RuntimeModulesBuilderExt =
      self.builder
  }

  implicit def ec: ExecutionContext

  def provider: ModulesExt

  def builder: RuntimeModulesBuilderExt

  def map: Map[Domain, RouteLike]

  implicit def system: ActorSystem =
    provider.akka.system

  implicit def timeout: Timeout =
    provider.config.timeout

  lazy val routes: Route =
    requestResponseHandler {
      reduceRouteMap(map)
    }

  private def reduceRouteMap(pathBindings: Map[Domain, RouteLike]): Route =
    pathBindings.map {
      case (k, v) =>
        pathPrefix(k)(v.routes)
    } reduce (_ ~ _)
}