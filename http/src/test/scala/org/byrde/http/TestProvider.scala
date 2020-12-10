package org.byrde.http

import org.byrde.http.server.Provider
import org.byrde.logging.Logger
import org.byrde.http.server.conf.AkkaHttpConfig

class TestProvider extends Provider {
  override def successCode: Int = 100
  
  override def config: AkkaHttpConfig =
    new TestAkkaHttpConfig
  
  override def logger: Logger =
    new TestLogger
}
