package org.byrde.client.redis

import scala.concurrent.duration.Duration

/** @param ttl
  *   Time to live in seconds
  * @tparam T
  *   Object type
  */
case class RedisObject[T](key: Key, `object`: T, ttl: Duration)
