package org.byrde.http.server.support

trait WithSuccessAndErrorCode {
  def successCode: Int
  
  def errorCode: Int = successCode + 1
}
