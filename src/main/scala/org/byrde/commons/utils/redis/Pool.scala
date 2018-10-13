package org.byrde.commons.utils.redis

import org.byrde.commons.utils.redis.conf.RedisConfig

import redis.clients.jedis.JedisPool

object Pool {
  def apply(config: RedisConfig): org.byrde.sedis.Pool = {
    val jedisPool =
      new JedisPool(
        config.poolConfig,
        config.host,
        config.port,
        config.timeout,
        config.password,
        config.database)

    new org.byrde.sedis.Pool(jedisPool)
  }
}
