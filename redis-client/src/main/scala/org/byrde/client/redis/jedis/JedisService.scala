package org.byrde.client.redis.jedis

import org.byrde.client.redis.jedis.conf.JedisConfig
import org.byrde.client.redis.{ Key, RedisClientError, RedisService }

import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._
import scala.util.Using

import redis.clients.jedis.JedisPool

/** Jedis-based implementation of RedisService.
  *
  * This implementation uses a connection pool to manage Redis connections efficiently.
  *
  * @param config
  *   Jedis configuration including host, port, and pool settings
  * @param ec
  *   Execution context for async operations
  */
class JedisService(val config: JedisConfig)(implicit ec: ExecutionContext) extends RedisService with AutoCloseable {
  private lazy val pool =
    new JedisPool(
      config.poolConfig,
      config.host,
      config.port,
      config.timeout,
      config.password,
      config.database,
    )

  override def keys(pattern: String): Future[Either[RedisClientError, Set[String]]] =
    Future {
      Using(pool.getResource) { jedis =>
        jedis.keys(pattern)
      }.map(_.asScala.toSet).toEither.left.map(RedisClientError.apply)
    }

  override def get(key: Key): Future[Either[RedisClientError, Option[String]]] =
    Future {
      Using(pool.getResource) { jedis =>
        Option(jedis.get(key))
      }.toEither.left.map(RedisClientError.apply)
    }

  override def set(key: Key, value: String, expiration: Duration): Future[Either[RedisClientError, Unit]] =
    Future {
      Using(pool.getResource) { jedis =>
        val expirationInSec =
          if (expiration == Duration.Inf)
            0
          else
            expiration.toSeconds.toLong

        jedis.set(key, value)

        if (expirationInSec != 0)
          jedis.expire(key, expirationInSec)

        ()
      }.toEither.left.map(RedisClientError.apply)
    }

  override def del(key: Key): Future[Either[RedisClientError, Long]] =
    Future {
      Using(pool.getResource) { jedis =>
        jedis.del(key)
      }.map(_.longValue()).toEither.left.map(RedisClientError.apply)
    }

  override def ttl(key: Key): Future[Either[RedisClientError, Long]] =
    Future {
      Using(pool.getResource) { jedis =>
        jedis.ttl(key)
      }.map(_.longValue).toEither.left.map(RedisClientError.apply)
    }

  /** Closes the connection pool and releases all resources.
    */
  def close(): Unit =
    if (pool != null) {
      pool.close()
    }
}

object JedisService {

  /** Creates a new JedisService with the given configuration.
    */
  def apply(config: JedisConfig)(implicit ec: ExecutionContext): JedisService = new JedisService(config)
}
