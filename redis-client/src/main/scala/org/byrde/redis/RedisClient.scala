package org.byrde.redis

import java.io._

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Printer}

import org.apache.commons.codec.binary.Base64

import zio.ZIO

import scala.concurrent.duration._
import scala.util.{Try, Using}

trait RedisClient[R <: RedisService] extends RedisExecutor[R] {

  private type Prefix = String
  private type DataStream = ByteArrayOutputStream

  def get[T](
    key: Key,
    withNamespace: Key => Namespace = key => s"global::$key"
  )(implicit decoder: Decoder[T]): ZIO[R, RedisClientError, Option[RedisObject[T]]] =
    for {
      env <- ZIO.environment[R]
      ttl <- redisClient.execute(_.ttl(withNamespace(key)).map(_.longValue().seconds)).provide(env)
      value <- redisClient.execute(_.get(withNamespace(key))).provide(env)
      result <- {
        value.map {
          case null =>
            ZIO.succeed(Option.empty[RedisObject[T]])

          case data =>
            ZIO
              .fromEither {
                processGetValue(data)
                  .map(RedisObject[T](key, _, ttl))
                  .map(Some.apply)
              }
        }
        .getOrElse(ZIO.succeed(Option.empty))
      }
    } yield result

  def set[T](
    key: Key,
    value: T,
    withNamespace: Key => Namespace = key => s"global::$key",
    expiration: Duration = Duration.Inf
  )(implicit encoder: Encoder[T], printer: Printer = Printer.noSpaces): ZIO[R, RedisClientError, Unit] =
    for {
      env <- ZIO.environment[R]
      redisK =
        withNamespace(key)
      (prefix, baos) =
        processSetValue(value)
      redisV =
        prefix + "-" + Using(baos)(data => new String(Base64.encodeBase64(data.toByteArray)))
      result <-
        redisClient.execute(_.set(redisK, redisV, expiration)).provide(env)
    } yield result

  def remove(key: Key): ZIO[R, RedisClientError, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- redisClient.execute(_.del(key)).provide(env)
    } yield result

  private def withDataInputStream[T](bytes: Array[Byte])(f: DataInputStream => T): Try[T] =
    Using(new DataInputStream(new ByteArrayInputStream(bytes)))(f)

  private def decodePrimitive[T](bytes: Array[Byte])(f: DataInputStream => T): Either[RedisClientError, T] =
    withDataInputStream(bytes)(f(_)).toEither.left.map(RedisClientError.apply)

  private def processGetValue[T](data: String)(implicit decoder: Decoder[T]): Either[RedisClientError, T] = {
    val innerData: Seq[String] =
      data.split("-").toIndexedSeq

    (innerData.head, Base64.decodeBase64(innerData.last)) match {
      case ("oos", bytes) =>
        withDataInputStream(bytes)(_.readUTF()).toEither.flatMap(_.asJson.as[T]).left.map(RedisClientError.apply)

      case ("string", bytes) =>
        decodePrimitive(bytes)(_.readUTF().asInstanceOf[T])

      case ("int", bytes) =>
        decodePrimitive(bytes)(_.readInt().asInstanceOf[T])

      case ("long", bytes) =>
        decodePrimitive(bytes)(_.readLong().asInstanceOf[T])

      case ("boolean", bytes) =>
        decodePrimitive(bytes)(_.readBoolean().asInstanceOf[T])

      case _ =>
        Left(RedisClientError(new Exception(s"Was not able to recognize the type of serialized value. The type was ${data.head}")))
    }
  }

  private def processSetValue[T](value: T)(implicit encoder: Encoder[T], printer: Printer): (Prefix, DataStream) =
    value match {
      case value: String =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(value.asInstanceOf[String])
        "string" -> baos

      case value: Int =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeInt(value.asInstanceOf[Int])
        "int" -> baos

      case value: Long =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeLong(value.asInstanceOf[Long])
        "long" -> baos

      case value: Boolean =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeBoolean(value.asInstanceOf[Boolean])
        "boolean" -> baos

      case value: T =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(value.asJson.printWith(printer))
        "oos" -> baos
    }

}