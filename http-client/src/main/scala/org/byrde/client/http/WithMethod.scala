package org.byrde.client.http

trait WithMethod[T <: WithMethod[T]] {
  
  def withMethod(method: Method): T
  
}

