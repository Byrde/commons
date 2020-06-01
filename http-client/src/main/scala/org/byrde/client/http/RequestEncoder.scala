package org.byrde.client.http

trait RequestEncoder[R, I, A] {
  
  def encode(request: I)(implicit env: R): A
  
}
