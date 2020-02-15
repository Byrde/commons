package org.byrde.akka.http.modules

import org.byrde.akka.http.scaladsl.server.directives.HttpRequestWithEntity

trait RuntimeModules[Req]

object RuntimeModules {
  trait RuntimeModulesBuilderLike[RuntimeModulesExt[T] <: RuntimeModules[T], ModulesExt <: ModulesProvider[RuntimeModulesExt]] {
    def apply[Req](provider: ModulesExt)(implicit req: HttpRequestWithEntity[Req]): RuntimeModulesExt[Req]
  }
}
