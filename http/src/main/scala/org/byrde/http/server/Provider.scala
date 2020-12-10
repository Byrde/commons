package org.byrde.http.server

import org.byrde.http.server.conf.AkkaHttpConfig
import org.byrde.http.server.support.WithSuccessAndErrorCode
import org.byrde.logging.Logger

trait Provider extends WithSuccessAndErrorCode {
  def config: AkkaHttpConfig
  
  def logger: Logger
}
