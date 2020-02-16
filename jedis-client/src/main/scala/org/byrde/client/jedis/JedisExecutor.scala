package org.byrde.client.jedis

import org.byrde.client.redis.{RedisClientError, RedisExecutor, RedisService}

import zio.{IO, ZIO}

trait JedisExecutor extends RedisExecutor[JedisService] {

  override val redisClient: RedisExecutor.Service[JedisService] =
    new RedisExecutor.Service[JedisService] {
      override def execute[T](request: RedisService => IO[RedisClientError, T]): ZIO[JedisService, RedisClientError, T] =
        for {
          redisClientLike <- ZIO.environment[JedisService]
          result <- request(redisClientLike)
        } yield result
    }

}
