package org.byrde.tapir

import org.byrde.logging.Logger
import org.byrde.tapir.conf.AkkaHttpConfig

class TestProvider extends Provider {
  override def SuccessCode: Int = 100
  
  override def ErrorCode: Int = 101
  
  override def config: AkkaHttpConfig =
    new TestAkkaHttpConfig
  
  override def logger: Logger =
    new TestLogger
}
