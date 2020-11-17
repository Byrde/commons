package org.byrde.tapir

import org.byrde.tapir.conf.CorsConfig

class TestCorsConfig extends CorsConfig {
  override def origins: Seq[String] = Seq.empty
  
  override def methods: Seq[String] = Seq.empty
  
  override def headers: Seq[String] = Seq.empty
}
