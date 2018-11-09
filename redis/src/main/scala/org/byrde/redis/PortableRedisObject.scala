package org.byrde.redis

import scala.concurrent.duration.Duration

case class PortableRedisObject[T](key: Key, `object`: T, ttl: Duration)