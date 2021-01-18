package org.byrde.http.server.support

trait CodeSupport {
  def successCode: Int
  
  def errorCode: Int = successCode + 1
}
