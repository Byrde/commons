package org.byrde.akka.http.modules

import org.byrde.akka.http.scaladsl.server.directives.HttpRequestWithEntity

trait RuntimeModulesLike[Req]

object RuntimeModulesLike {
  trait RuntimeModulesBuilderLike[RuntimeModulesExt[T] <: RuntimeModulesLike[T], ModulesExt <: ModulesProviderLike[RuntimeModulesExt]] {
    def apply[Req](provider: ModulesExt)(implicit req: HttpRequestWithEntity[Req]): RuntimeModulesExt[Req]
  }
}
