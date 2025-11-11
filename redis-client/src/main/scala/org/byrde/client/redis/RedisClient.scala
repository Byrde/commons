package org.byrde.client.redis

import io.circe.{ Decoder, Encoder }

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** High-level Redis client interface with type-safe serialization.
  *
  * Provides operations for storing and retrieving typed values in Redis. Implementations handle the low-level Redis
  * operations and serialization.
  */
trait RedisClient {

  /** Retrieves a value from Redis.
    *
    * @param key
    *   The key to retrieve
    * @param withNamespace
    *   Function to namespace the key (default: "global::{key}")
    * @param decoder
    *   Implicit decoder for the value type
    * @tparam T
    *   The value type
    * @return
    *   Future with Either containing the RedisObject with TTL, None if not found, or an error
    */
  def get[T](
    key: Key,
    withNamespace: Key => String = key => s"global::$key",
  )(implicit decoder: Decoder[T]): Future[Either[RedisClientError, Option[RedisObject[T]]]]

  /** Stores a value in Redis.
    *
    * @param key
    *   The key to store under
    * @param value
    *   The value to store
    * @param withNamespace
    *   Function to namespace the key (default: "global::{key}")
    * @param expiration
    *   TTL for the key (default: no expiration)
    * @param encoder
    *   Implicit encoder for the value type
    * @tparam T
    *   The value type
    * @return
    *   Future with Either containing Unit on success or an error
    */
  def set[T](
    key: Key,
    value: T,
    withNamespace: Key => String = key => s"global::$key",
    expiration: Duration = Duration.Inf,
  )(implicit encoder: Encoder[T]): Future[Either[RedisClientError, Unit]]

  /** Removes a key from Redis.
    *
    * @param key
    *   The key to remove
    * @param withNamespace
    *   Function to namespace the key (default: "global::{key}")
    * @return
    *   Future with Either containing the number of keys removed or an error
    */
  def remove(
    key: Key,
    withNamespace: Key => String = key => s"global::$key",
  ): Future[Either[RedisClientError, Long]]
}
