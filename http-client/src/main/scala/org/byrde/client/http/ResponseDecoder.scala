package org.byrde.client.http

import zio.IO

trait ResponseDecoder[R, I, A] {
  
  def decode[T](response: I)(request: Request[T])(implicit env: R): IO[HttpClientError, A]
  
}
