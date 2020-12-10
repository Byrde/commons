package org.byrde.http.server.conf

import com.typesafe.config.Config

trait AkkaHttpConfig {
  def name: String
  
  def version: String

  def interface: String

  def port: Int
  
  def corsConfig: CorsConfig
}

object AkkaHttpConfig {
  def apply(config: Config): AkkaHttpConfig =
    apply("name", "version", "interface", "port", "cors", config)
  
  def apply(
    name: String,
    version: String,
    interface: String,
    port: String,
    cors: String,
    config: Config
  ): AkkaHttpConfig = {
    val _name =
      config.getString(name)
    
    val _version =
      config.getString(version)
    
    val _interface =
      config.getString(interface)
    
    val _port =
      config.getInt(port)
    
    val _cors =
      CorsConfig(config.getConfig(cors))
    
    new AkkaHttpConfig {
      override def name: String = _name
  
      override def version: String = _version
  
      override def interface: String = _interface
  
      override def port: Int = _port
  
      override def corsConfig: CorsConfig = _cors
    }
  }
}