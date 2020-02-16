package org.byrde.client.jedis

import org.byrde.client.redis.RedisClient

trait JedisClient extends RedisClient[JedisService] with JedisExecutor