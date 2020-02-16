package org.byrde.client.redis

case class RedisClientError(throwable: Throwable) extends Throwable(throwable)
