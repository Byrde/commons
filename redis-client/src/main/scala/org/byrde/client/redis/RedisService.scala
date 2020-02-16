package org.byrde.client.redis

import zio.IO

import scala.concurrent.duration.Duration

trait RedisService {

  def keys(pattern: String): IO[RedisClientError, Set[String]]

  def get(key: Key): IO[RedisClientError, Option[String]]

  def set(key: Key, value: String, expiration: Duration = Duration.Inf): IO[RedisClientError, Unit]

  def del(key: Key): IO[RedisClientError, Long]

  def ttl(key: Key): IO[RedisClientError, Long]

}

