package org.byrde.jedis

import org.byrde.redis.{Key, Namespace, RedisClientError, RedisObject}

import io.circe.{Decoder, Encoder}

import zio.IO

import scala.concurrent.duration.Duration

abstract class WiredJedisClient(client: JedisService) extends JedisClient {

  def wiredGet[T](
    key: Key,
    withNamespace: Key => Namespace = key => s"global::$key"
  )(implicit decoder: Decoder[T]): IO[RedisClientError, Option[RedisObject[T]]] =
    get(key, withNamespace).provide(client)

  def wiredSet[T](
    key: Key,
    value: T,
    withNamespace: Key => Namespace = key => s"global::$key",
    expiration: Duration = Duration.Inf
  )(implicit encoder: Encoder[T]): IO[RedisClientError, Unit] =
    set(key, value, withNamespace, expiration).provide(client)

  def wiredRemove(
    key: Key
  ): IO[RedisClientError, Long] =
    remove(key).provide(client)

}
