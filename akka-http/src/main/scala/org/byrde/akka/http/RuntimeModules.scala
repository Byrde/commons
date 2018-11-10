package org.byrde.akka.http

import org.byrde.akka.http.scaladsl.server.directives.HttpRequestWithEntity

import scala.language.higherKinds

trait RuntimeModules[Req]

object RuntimeModules {
  trait RuntimeModulesBuilder[RuntimeModulesExt[T] <: RuntimeModules[T], ModulesExt <: ModulesProvider[RuntimeModulesExt]] {
    def apply[Req](provider: ModulesExt)(implicit req: HttpRequestWithEntity[Req]): RuntimeModulesExt[Req]
  }
}
