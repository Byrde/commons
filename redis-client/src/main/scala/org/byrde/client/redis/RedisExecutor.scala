package org.byrde.client.redis

import scala.concurrent.Future

trait RedisExecutor[R] {
  def executor: RedisExecutor.Service[R]
}

object RedisExecutor {
  trait Service[R] {
    def execute[T](request: RedisService => Future[Either[RedisClientError, T]]): Future[Either[RedisClientError, T]]
  }
}
