package org.byrde.akka.http

import org.byrde.akka.http.conf.CORSConfig
import org.byrde.akka.http.logging.{HttpErrorLogging, HttpRequestLogging}
import org.byrde.akka.http.modules.RuntimeModulesLike.RuntimeModulesBuilderLike
import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}
import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.RequestResponseHandlingSupport
import org.byrde.service.response.CommonsServiceResponseDictionary.E0200

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait ServerLike [
  RuntimeModulesExt[T] <: RuntimeModulesLike[T],
  ModulesExt <: ModulesProviderLike[RuntimeModulesExt]
] extends RequestResponseHandlingSupport {
  self =>

  type Domain = String

  type Version = String

  trait RuntimeModulesMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt] {
    override lazy val provider: ModulesExt =
      self.provider

    override lazy val builder: RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt] =
      self.builder
  }

  trait RouteMixin extends RouteLike {
    override lazy val SuccessCode: Int =
      self.SuccessCode
  }

  implicit def global: ExecutionContext

  def provider: ModulesExt

  def builder: RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt]

  def map: Map[Version, Map[Domain, RouteLike]]

  implicit def system: ActorSystem =
    provider.akka.system

  implicit def timeout: Timeout =
    provider.config.timeout

  lazy val SuccessCode: Int =
    provider.SuccessCode

  lazy val ErrorCode: Int =
    provider.ErrorCode

  lazy val ping: Route =
    path("ping") {
      get {
        complete(E0200("Pong!"))
      }
    }

  lazy val routes: Route =
    requestResponseHandler {
      ping ~ reduceRouteMap(map)
    }

  lazy val RequestLogger: HttpRequestLogging =
    provider.RequestLogger

  lazy val ErrorLogger: HttpErrorLogging =
    provider.ErrorLogger

  lazy val CORSConfig: CORSConfig =
    provider.config.cors

  def reduceRouteMap(pathBindings: Map[Version, Map[Domain, RouteLike]]): Route =
    pathBindings.map {
      case (k, v) =>
        pathPrefix(k) {
          v.map {
            case (k2, v2) =>
              pathPrefix(k2) {
                v2.route
              }
          } reduce (_ ~ _)
        }
    } reduce (_ ~ _)
}