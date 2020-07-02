package org.byrde.client.http

trait ResponseDecoder[R, I, A] {
  
  def decode[T](request: Request[T])(response: I)(implicit env: R): Either[HttpClientError, A]
  
}
