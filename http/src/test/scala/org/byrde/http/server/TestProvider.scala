package org.byrde.http.server

import org.byrde.http.server.conf.AkkaHttpConfig
import org.byrde.logging.Logger

class TestProvider extends Provider {
  override def successCode: Int = 100
  
  override def config: AkkaHttpConfig =
    new TestAkkaHttpConfig
  
  override def logger: Logger =
    new TestLogger
}
