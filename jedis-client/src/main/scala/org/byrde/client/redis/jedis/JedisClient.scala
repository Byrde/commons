package org.byrde.client.redis.jedis

import org.byrde.client.redis.{RedisClient, RedisClientError, RedisExecutor, RedisService}

import zio.{IO, ZIO}

class JedisClient extends RedisClient[JedisService] {
  
  override val executor: RedisExecutor.Service[JedisService] =
    new RedisExecutor.Service[JedisService] {
      override def execute[T](request: RedisService => IO[RedisClientError, T]): ZIO[JedisService, RedisClientError, T] =
        for {
          redisClientLike <- ZIO.environment[JedisService]
          result <- request(redisClientLike)
        } yield result
    }
  
}