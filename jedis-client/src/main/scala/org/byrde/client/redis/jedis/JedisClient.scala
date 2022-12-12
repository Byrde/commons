package org.byrde.client.redis.jedis

import org.byrde.client.redis.{ RedisClient, RedisClientError, RedisExecutor, RedisService }

import scala.concurrent.{ ExecutionContext, Future }

class JedisClient(env: JedisService)(implicit ec: ExecutionContext) extends RedisClient[JedisService] {
  override val executor: RedisExecutor.Service[JedisService] =
    new RedisExecutor.Service[JedisService] {
      override def execute[T](
        request: RedisService => Future[Either[RedisClientError, T]],
      ): Future[Either[RedisClientError, T]] = request(env)
    }
}
