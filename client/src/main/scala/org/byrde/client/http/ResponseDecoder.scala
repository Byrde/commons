package org.byrde.client.http

import zio.IO

trait ResponseDecoder[R, I, A] {
  def decode[T <: RequestLike](response: I, fail: Boolean)(request: T)(implicit env: R): IO[HttpClientError, A]
}
