package org.byrde.tapir

import org.byrde.tapir.conf.{AkkaHttpConfig, CorsConfig}

class TestAkkaHttpConfig extends AkkaHttpConfig {
  override def name: String = "test"
  
  override def version: String = "1.0"
  
  override def interface: String = "0.0.0.0"
  
  override def port: Int = 80
  
  override def cors: CorsConfig = new TestCorsConfig
}
