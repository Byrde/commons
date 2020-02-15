package org.byrde.client.http

import zio.ZIO

trait HttpExecutor[R, I, A] {
  def httpClient: HttpExecutor.Service[R, I, A]
}

object HttpExecutor {
  trait Service[R, I, A] {
    def execute[T <: RequestLike](request: I): ZIO[R, HttpClientError, A]
  }
}