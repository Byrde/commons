package org.byrde.tapir

import org.byrde.logging.Logger
import org.byrde.tapir.conf.AkkaHttpConfig
import org.byrde.tapir.support.WithSuccessAndErrorCode

trait Provider extends WithSuccessAndErrorCode {
  def config: AkkaHttpConfig
  
  def logger: Logger
}
