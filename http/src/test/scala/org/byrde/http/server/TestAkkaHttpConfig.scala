package org.byrde.http.server

import org.byrde.http.server.conf.{AkkaHttpConfig, CorsConfig}

class TestAkkaHttpConfig extends AkkaHttpConfig {
  override def name: String = "test"
  
  override def version: String = "1.0"
  
  override def interface: String = "0.0.0.0"
  
  override def port: Int = 80
  
  override def corsConfig: CorsConfig = new TestCorsConfig
}
