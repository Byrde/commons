package org.byrde.akka.http

import org.byrde.akka.http.conf.CORSConfig
import org.byrde.akka.http.logging.{HttpErrorLogging, HttpRequestLogging}
import org.byrde.akka.http.modules.RuntimeModulesLike.RuntimeModulesBuilderLike
import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}
import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.{RequestResponseHandlingSupport, RejectionSupport}
import org.byrde.service.response.CommonsServiceResponseDictionary.E0200

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.util.Timeout

import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait ServerLike[
  RuntimeModulesExt[T] <: RuntimeModulesLike[T],
  ModulesExt <: ModulesProviderLike[RuntimeModulesExt]
] extends RequestResponseHandlingSupport {
  self =>

  trait RuntimeModulesMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt] {
    override lazy val provider: ModulesExt =
      self.provider

    override lazy val builder: RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt] =
      self.builder
  }

  trait RouteMixin extends RejectionSupport {
    override def SuccessCode: Int =
      self.SuccessCode

    override def ErrorLogger: HttpErrorLogging =
      provider.ErrorLogger
  }

  implicit def global: ExecutionContext

  def provider: ModulesExt

  def builder: RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt]

  def routes: Route

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
        complete(E0200("Pong!").toJson)
      }
    }

  lazy val RequestLogger: HttpRequestLogging =
    provider.RequestLogger

  lazy val ErrorLogger: HttpErrorLogging =
    provider.ErrorLogger

  lazy val CORSConfig: CORSConfig =
    provider.config.cors

  lazy val HandledRoutes: Route =
    requestResponseHandler {
      ping ~ routes
    }
}