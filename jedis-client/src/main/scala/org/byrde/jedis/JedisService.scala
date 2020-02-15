package org.byrde.jedis

import org.byrde.jedis.conf.JedisConfig
import org.byrde.redis.{Key, RedisClientError, RedisService}

import com.typesafe.config.Config

import redis.clients.jedis.JedisPool

import zio.{IO, ZIO}

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import scala.util.Using

trait JedisService extends RedisService

object JedisService {

  def apply(config: Config): JedisService =
    apply(JedisConfig(config))

  def apply(config: JedisConfig): JedisService = {
    val pool =
      new JedisPool(
        config.poolConfig,
        config.host,
        config.port,
        config.timeout,
        config.password,
        config.database
      )

    new JedisService {
      override def keys(pattern: String): IO[RedisClientError, Set[String]] =
        ZIO.fromTry {
          Using(pool.getResource) { jedis =>
            jedis.keys(pattern)
          }
        }
        .map(_.asScala.toSet)
        .mapError(RedisClientError.apply)

      override def get(key: Key): IO[RedisClientError, Option[String]] =
        ZIO.fromTry {
          Using(pool.getResource) { jedis =>
            Option(jedis.get(key))
          }
        }
        .mapError(RedisClientError.apply)

      override def set(key: Key, value: String, expiration: Duration): IO[RedisClientError, Unit] =
        ZIO.fromTry {
          Using(pool.getResource) { jedis =>
            val expirationInSec =
              if (expiration == Duration.Inf)
                0
              else
                expiration.toSeconds.toInt

            jedis.set(key, value)

            if (expirationInSec != 0)
              jedis.expire(key, expirationInSec)

            ()
          }
        }
        .mapError(RedisClientError.apply)

      override def del(key: Key): IO[RedisClientError, Long] =
        ZIO.fromTry {
          Using(pool.getResource) { jedis =>
            jedis.del(key)
          }
        }
        .map(_.longValue())
        .mapError(RedisClientError.apply)

      override def ttl(key: Key): IO[RedisClientError, Long] =
        ZIO.fromTry {
          Using(pool.getResource) { jedis =>
            jedis.ttl(key)
          }
        }
        .map(_.longValue())
        .mapError(RedisClientError.apply)
    }
  }

}