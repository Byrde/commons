package org.byrde.redis

import scala.concurrent.duration.Duration

case class RedisObject[T](key: Key, `object`: T, ttl: Duration)