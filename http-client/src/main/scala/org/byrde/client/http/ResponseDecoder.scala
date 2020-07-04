package org.byrde.client.http

trait ResponseDecoder[R, T, I, A] {
  
  def decode(request: T)(response: I)(implicit env: R): Either[HttpClientError, A]
  
}
