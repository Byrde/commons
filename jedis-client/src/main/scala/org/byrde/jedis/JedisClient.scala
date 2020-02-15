package org.byrde.jedis

import org.byrde.redis.RedisClient

trait JedisClient extends RedisClient[JedisService] with JedisExecutor