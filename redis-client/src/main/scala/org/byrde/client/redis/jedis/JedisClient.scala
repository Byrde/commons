package org.byrde.client.redis.jedis

import org.byrde.client.redis.{ BaseRedisClient, RedisService }

import scala.concurrent.ExecutionContext

/** Jedis-based implementation of RedisClient.
  *
  * This implementation uses the Jedis library to connect to Redis via the Redis protocol.
  *
  * @param jedisService
  *   The JedisService providing the connection pool
  * @param ec
  *   Execution context for async operations
  */
class JedisClient(jedisService: JedisService)(implicit ec: ExecutionContext) extends BaseRedisClient {
  override protected def service: RedisService = jedisService
}

object JedisClient {

  /** Creates a new JedisClient with the given configuration.
    */
  def apply(config: org.byrde.client.redis.jedis.conf.JedisConfig)(implicit ec: ExecutionContext): JedisClient =
    new JedisClient(new JedisService(config))
}
