package org.byrde.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.util.Timeout

import org.byrde.akka.http.conf.CORSConfig
import org.byrde.akka.http.modules.RuntimeModules.RuntimeModulesBuilderLike
import org.byrde.akka.http.modules.{ModulesProvider, RuntimeModules}
import org.byrde.akka.http.scaladsl.server.directives.UnmarshallingRuntimeModulesDirective
import org.byrde.akka.http.support.{RequestResponseHandlingSupport, ResponseSupport}
import org.byrde.logging.Logger
import org.byrde.service.response.DefaultEmptyServiceResponse
import org.byrde.service.response.Status.S0200

import io.circe.generic.auto._
import io.circe.{Json, Printer}

import scala.concurrent.ExecutionContext

trait Server[
  RuntimeModulesExt[T] <: RuntimeModules[T],
  ModulesExt <: ModulesProvider[RuntimeModulesExt]
] extends RequestResponseHandlingSupport {
  self =>

  override implicit lazy val printer: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  trait RuntimeModulesMixin extends UnmarshallingRuntimeModulesDirective[RuntimeModulesExt, ModulesExt] {
    override def provider: ModulesExt =
      self.provider

    override def builder: RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt] =
      self.builder
  }

  trait RouteMixin extends ResponseSupport {
    override def SuccessCode: Int =
      self.SuccessCode

    override def logger: Logger =
      provider.logger

    override def Ack: Json =
      self.Ack

    override implicit def printer: Printer =
      self.printer
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

  lazy val Ack: Json =
    new DefaultEmptyServiceResponse(S0200, self.SuccessCode).toJson

  lazy val ping: Route =
    path("ping") {
      get {
        complete(Ack)
      }
    }

  lazy val logger: Logger =
    provider.logger

  lazy val cors: CORSConfig =
    provider.config.cors

  lazy val handled: Route =
    requestResponseHandler {
      ping ~ routes
    }

}
