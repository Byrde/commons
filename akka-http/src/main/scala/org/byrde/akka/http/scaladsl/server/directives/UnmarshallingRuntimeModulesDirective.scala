package org.byrde.akka.http.scaladsl.server.directives

import org.byrde.akka.http.modules.{ModulesProvider, RuntimeModules}

import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

import io.circe.Decoder

import scala.reflect.ClassTag

trait UnmarshallingRuntimeModulesDirective [
  RuntimeModulesExt[T] <: RuntimeModules[T],
  ModulesExt <: ModulesProvider[RuntimeModulesExt]
] extends UnmarshallingRequestWithJsonRequestDirective {

  def provider: ModulesExt

  def builder: RuntimeModules.RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt]

  override def requestWithEntity[T](um: FromEntityUnmarshaller[T]): Directive1[HttpRequestWithEntity[T]] =
    super.requestWithEntity(um).tflatMap(tup => appendAttrs(tup._1))

  override def requestWithJsonEntity[T: ClassTag](errorCode: Int)(fn: HttpRequestWithEntity[T] => Route)(implicit decoder: Decoder[T]): Route =
    super.requestWithJsonEntity[T](errorCode)(request => appendAttrs[T](request)(fn))

  private def appendAttrs[Req](requestWithEntity: HttpRequestWithEntity[Req]): Directive1[HttpRequestWithEntity[Req]] =
    provide(provider.withModulesAttr(builder(provider)(requestWithEntity))(requestWithEntity))

}
