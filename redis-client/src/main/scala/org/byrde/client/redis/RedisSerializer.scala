package org.byrde.client.redis

import io.circe.{ Decoder, Encoder }

/** Trait for serializing and deserializing values to/from Redis.
  */
trait RedisSerializer {

  /** Serializes a value to a string for storage in Redis.
    *
    * @param value
    *   The value to serialize
    * @tparam T
    *   The value type
    * @return
    *   Either containing RedisClientError on failure or the serialized string on success
    */
  def serialize[T](value: T)(implicit encoder: Encoder[T]): Either[RedisClientError, String]

  /** Deserializes a string from Redis back to a value.
    *
    * @param data
    *   The serialized string
    * @tparam T
    *   The value type
    * @return
    *   Either containing RedisClientError on failure or the deserialized value on success
    */
  def deserialize[T](data: String)(implicit decoder: Decoder[T]): Either[RedisClientError, T]
}
