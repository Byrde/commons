package org.byrde.redis
import org.byrde.redis.conf.RedisConfig

import redis.clients.jedis.{Jedis, JedisPool}

class Pool(val underlying: JedisPool) {
  def withClient[T](body: Dress.Wrap => T): T = {
    val jedis: Jedis =
      underlying.getResource

    try
      body(Dress.up(jedis))
    finally
      underlying.close()
  }

  def withJedisClient[T](body: Jedis => T): T = {
    val jedis: Jedis =
      underlying.getResource

    try
      body(jedis)
    finally
      underlying.close()
  }
}

object Pool {
  def apply(config: RedisConfig): Pool = {
    val jedisPool =
      new JedisPool(
        config.poolConfig,
        config.host,
        config.port,
        config.timeout,
        config.password,
        config.database)

    new Pool(jedisPool)
  }
}
