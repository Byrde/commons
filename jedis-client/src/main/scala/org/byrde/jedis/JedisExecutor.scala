package org.byrde.jedis

import org.byrde.redis.{RedisClientError, RedisService, RedisExecutor}

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
