package org.byrde.tapir

import org.byrde.logging.Logger
import org.byrde.tapir.conf.AkkaHttpConfig

trait Provider {
  def SuccessCode: Int
  
  def ErrorCode: Int
  
  def config: AkkaHttpConfig
  
  def logger: Logger
}
