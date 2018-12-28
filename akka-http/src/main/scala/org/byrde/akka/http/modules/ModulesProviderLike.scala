package org.byrde.akka.http.modules

import org.byrde.akka.http.conf.ConfigLike
import org.byrde.akka.http.libs.typedmap.{TypedEntry, TypedKey}
import org.byrde.akka.http.logging.{HttpErrorLogging, HttpRequestLogging}
import org.byrde.akka.http.scaladsl.server.directives.HttpRequestWithEntity

import scala.language.higherKinds

trait ModulesProviderLike[RuntimeModulesExt[T] <: RuntimeModulesLike[T]] {
  def SuccessCode: Int

  def ErrorCode: Int

  def config: ConfigLike

  def akka: AkkaLike

  def RequestLogger: HttpRequestLogging

  def ErrorLogger: HttpErrorLogging

  def ModulesAttr[Req]: TypedKey[RuntimeModulesExt[Req]] =
    TypedKey[RuntimeModulesExt[Req]]("Modules")

  /**
   * Distinction is made between [[ModulesProviderLike]] & [[RuntimeModulesExt]].
   * [[RuntimeModulesExt]] is initialized and dependent on a request, this is useful
   * for initalizing and containing class members that will have cached assets that can't be
   * shared between requests. [[ModulesProviderLike]] is a subset of [[RuntimeModulesExt]] where
   * the class members can be shared between requests, e.g injected members.
   *
   * @param req - The request to bind this instance of [[RuntimeModulesExt]] to.
   * @return ModulesProvider with request applied to initialize class members dependent on the request
   */
  def apply[Req](req: HttpRequestWithEntity[Req]): RuntimeModulesExt[Req] =
    req.getAttr(ModulesAttr[Req]).getOrElse(throw new Exception(s"Binding error `RuntimeModules` to request"))

  def withModulesAttr[Req](modules: RuntimeModulesExt[Req])(req: HttpRequestWithEntity[Req]): HttpRequestWithEntity[Req] =
    req.withAttr(TypedEntry(ModulesAttr[Req], modules))
}