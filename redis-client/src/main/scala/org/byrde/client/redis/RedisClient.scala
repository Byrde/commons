package org.byrde.client.redis

import java.io._

import org.byrde.support.EitherSupport

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Printer}
import org.apache.commons.codec.binary.Base64

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Using}

abstract class RedisClient[R <: RedisService](implicit ec: ExecutionContext) extends RedisExecutor[R] with EitherSupport {

  private type Namespace = String

  private type Prefix = String

  private type DataStream = ByteArrayOutputStream

  def get[T](
    key: Key,
    withNamespace: Key => Namespace = key => s"global::$key"
  )(implicit decoder: Decoder[T]): Future[Either[RedisClientError, Option[RedisObject[T]]]] =
    for {
      ttl <- executor.execute(_.ttl(withNamespace(key)).map(_.map(_.longValue().seconds)))
      value <- executor.execute(_.get(withNamespace(key)))
      result =
        value.zip(ttl).flatMap {
          case (None, _) =>
            Right(Option.empty[RedisObject[T]])

          case (Some(data), ttl) =>
            processGetValue(data)
              .map(RedisObject[T](key, _, ttl))
              .map(Some.apply)
        }
    } yield result

  def set[T](
    key: Key,
    value: T,
    withNamespace: Key => Namespace = key => s"global::$key",
    expiration: Duration = Duration.Inf
  )(implicit encoder: Encoder[T], printer: Printer = Printer.noSpaces): Future[Either[RedisClientError, Unit]] = {
    val redisK =
      withNamespace(key)

    val (prefix, baos) =
      processSetValue(value)

    val redisV =
      prefix + "-" + Using(baos)(data => new String(Base64.encodeBase64(data.toByteArray)))

    executor.execute(_.set(redisK, redisV, expiration))
  }

  def remove(
    key: Key,
    withNamespace: Key => Namespace = key => s"global::$key"
  ): Future[Either[RedisClientError, Long]] =
    executor.execute(_.del(withNamespace(key)))

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

      case value =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(value.asJson.printWith(printer))
        "oos" -> baos
    }

}