package org.byrde.client.redis


import scala.concurrent.duration.Duration
import scala.concurrent.Future

trait RedisService {

  def keys(pattern: String): Future[Either[RedisClientError, Set[String]]]

  def get(key: Key): Future[Either[RedisClientError, Option[String]]]

  def set(key: Key, value: String, expiration: Duration = Duration.Inf): Future[Either[RedisClientError, Unit]]

  def del(key: Key): Future[Either[RedisClientError, Long]]

  def ttl(key: Key): Future[Either[RedisClientError, Long]]

}

