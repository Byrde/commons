package org.byrde.client.redis.serialization

import org.byrde.client.redis.{ RedisClientError, RedisSerializer }

import io.circe.parser._
import io.circe.syntax._
import io.circe.{ Decoder, Encoder, Printer }

import java.io._
import java.util.Base64

import scala.util.{ Try, Using }

/** Redis serializer that uses Circe for JSON encoding/decoding.
  *
  * Supports primitive types (String, Int, Long, Boolean) with optimized encoding, and falls back to JSON for complex
  * types.
  */
class CirceRedisSerializer extends RedisSerializer {

  private val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  override def serialize[T](value: T)(implicit encoder: Encoder[T]): Either[RedisClientError, String] = {
    val (prefix, baos) = serializeValue(value)

    Using(baos) { data =>
      val encoded = Base64.getEncoder.encodeToString(data.toByteArray)
      s"$prefix-$encoded"
    }.toEither.left.map(RedisClientError.apply)
  }

  override def deserialize[T](data: String)(implicit decoder: Decoder[T]): Either[RedisClientError, T] = {
    val parts = data.split("-", 2)
    if (parts.length != 2) {
      return Left(RedisClientError(new IllegalArgumentException(s"Invalid serialized format: $data")))
    }

    val (typePrefix, encodedData) = (parts(0), parts(1))

    Try {
      Base64.getDecoder.decode(encodedData)
    }.toEither.left.map(RedisClientError.apply).flatMap(bytes => deserializeValue(typePrefix, bytes))
  }

  private def serializeValue[T](value: T)(implicit encoder: Encoder[T]): (String, ByteArrayOutputStream) =
    value match {
      case v: String =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(v)
        ("string", baos)

      case v: Int =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeInt(v)
        ("int", baos)

      case v: Long =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeLong(v)
        ("long", baos)

      case v: Boolean =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeBoolean(v)
        ("boolean", baos)

      case v =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(v.asJson.printWith(printer))
        ("json", baos)
    }

  private def deserializeValue[T](typePrefix: String, bytes: Array[Byte])(
    implicit decoder: Decoder[T],
  ): Either[RedisClientError, T] =
    typePrefix match {
      case "json" =>
        withDataInputStream(bytes)(_.readUTF())
          .toEither
          .left
          .map(RedisClientError.apply)
          .flatMap(parse(_).left.map(RedisClientError.apply))
          .flatMap(_.as[T].left.map(RedisClientError.apply))

      case "string" =>
        decodePrimitive(bytes)(_.readUTF().asInstanceOf[T])

      case "int" =>
        decodePrimitive(bytes)(_.readInt().asInstanceOf[T])

      case "long" =>
        decodePrimitive(bytes)(_.readLong().asInstanceOf[T])

      case "boolean" =>
        decodePrimitive(bytes)(_.readBoolean().asInstanceOf[T])

      case unknown =>
        Left(
          RedisClientError(
            new IllegalArgumentException(s"Unknown type prefix: $unknown"),
          ),
        )
    }

  private def withDataInputStream[T](bytes: Array[Byte])(f: DataInputStream => T): Try[T] =
    Using(new DataInputStream(new ByteArrayInputStream(bytes)))(f)

  private def decodePrimitive[T](bytes: Array[Byte])(f: DataInputStream => T): Either[RedisClientError, T] =
    withDataInputStream(bytes)(f).toEither.left.map(RedisClientError.apply)
}
