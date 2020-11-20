package org.byrde.tapir.support

trait WithSuccessAndErrorCode {
  def successCode: Int
  
  def errorCode: Int = successCode + 1
}
