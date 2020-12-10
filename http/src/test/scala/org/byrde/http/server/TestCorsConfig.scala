package org.byrde.http.server

import org.byrde.http.server.conf.CorsConfig

class TestCorsConfig extends CorsConfig {
  override def origins: Seq[String] = Seq.empty
  
  override def methods: Seq[String] = Seq.empty
  
  override def headers: Seq[String] = Seq.empty
}
