package org.byrde.http.server.support

import scala.util.ChainingSyntax

trait HttpStatus2TapirStatusSupport extends ChainingSyntax {
  implicit val httpStatus2TapirStatus: akka.http.scaladsl.model.StatusCode => sttp.model.StatusCode =
    _.intValue.pipe(sttp.model.StatusCode.apply)
}
