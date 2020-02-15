package org.byrde.redis

case class RedisClientError(throwable: Throwable) extends Throwable(throwable)
