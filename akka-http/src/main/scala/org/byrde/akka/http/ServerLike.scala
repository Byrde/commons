package org.byrde.akka.http

import org.byrde.akka.http.conf.CORSConfig
import org.byrde.akka.http.logging.{HttpErrorLogging, HttpRequestLogging}
import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}
import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.RequestResponseHandlingSupport

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
] extends RequestResponseHandlingSupport {
  self =>

  type Domain = String

  trait RuntimeModulesMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt, RuntimeModulesBuilderExt] {
    override lazy val provider: ModulesExt =
      self.provider

    override lazy val builder: RuntimeModulesBuilderExt =
      self.builder
  }

  trait RouteMixin extends RouteLike[RuntimeModulesExt, ModulesExt] {
    override lazy val SuccessCode: Int =
      self.SuccessCode
  }

  implicit def global: ExecutionContext

  def provider: ModulesExt

  def builder: RuntimeModulesBuilderExt

  def map: Map[Domain, RouteLike[RuntimeModulesExt, ModulesExt]]

  implicit def system: ActorSystem =
    provider.akka.system

  implicit def timeout: Timeout =
    provider.config.timeout

  lazy val SuccessCode: Int =
    provider.SuccessCode

  lazy val ErrorCode: Int =
    provider.ErrorCode

  lazy val routes: Route =
    requestResponseHandler {
      reduceRouteMap(map)
    }

  lazy val RequestLogger: HttpRequestLogging =
    provider.RequestLogger

  lazy val ErrorLogger: HttpErrorLogging =
    provider.ErrorLogger

  lazy val CORSConfig: CORSConfig =
    provider.config.cors

  private def reduceRouteMap(pathBindings: Map[Domain, RouteLike[RuntimeModulesExt, ModulesExt]]): Route =
    pathBindings.map {
      case (k, v) =>
        pathPrefix(k)(v.routes)
    } reduce (_ ~ _)
}