package org.byrde.client.redis

import zio.{IO, ZIO}

trait RedisExecutor[R] {
  def executor: RedisExecutor.Service[R]
}

object RedisExecutor {
  trait Service[R] {
    def execute[T](request: RedisService => IO[RedisClientError, T]): ZIO[R, RedisClientError, T]
  }
}
