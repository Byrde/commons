package org.byrde.akka.http.scaladsl.server.directives

import org.byrde.akka.http.modules.{ModulesProviderLike, RuntimeModulesLike}

import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

import play.api.libs.json.Reads

import scala.language.higherKinds
import scala.reflect.ClassTag

trait UnmarshallingRuntimeModulesDirective [
  RuntimeModulesExt[T] <: RuntimeModulesLike[T],
  ModulesExt <: ModulesProviderLike[RuntimeModulesExt]
] extends UnmarshallingRequestWithJsonRequestDirective {
  def provider: ModulesExt

  def builder: RuntimeModulesLike.RuntimeModulesBuilderLike[RuntimeModulesExt, ModulesExt]

  override def requestWithEntity[T](um: FromEntityUnmarshaller[T]): Directive1[HttpRequestWithEntity[T]] =
    super.requestWithEntity(um).tflatMap(tup => appendAttrs(tup._1))

  override def requestWithJsonEntity[T: ClassTag](errorCode: Int)(fn: HttpRequestWithEntity[T] => Route)(implicit reads: Reads[T]): Route =
    super.requestWithJsonEntity[T](errorCode)(request => appendAttrs[T](request)(fn))

  private def appendAttrs[Req](requestWithEntity: HttpRequestWithEntity[Req]): Directive1[HttpRequestWithEntity[Req]] =
    provide(provider.withModulesAttr(builder(provider)(requestWithEntity))(requestWithEntity))
}
