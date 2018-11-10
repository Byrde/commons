package org.byrde.akka.http

import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.{RequestResponseHandlingSupport, RouteSupport}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait Server [
  RuntimeModulesExt[T] <: RuntimeModules[T],
  ModulesExt <: ModulesProvider[RuntimeModulesExt],
  RuntimeModulesBuilderExt <: RuntimeModules.RuntimeModulesBuilder[RuntimeModulesExt, ModulesExt]
] extends RouteSupport with RequestResponseHandlingSupport {
  self =>

  trait RuntimeModulesDirectiveMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt, RuntimeModulesBuilderExt] {
    override lazy val provider: ModulesExt =
      self.provider

    override lazy val builder: RuntimeModulesBuilderExt =
      self.builder
  }

  type Domain = String

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