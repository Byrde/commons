package org.byrde.redis

import java.io._

import org.byrde.redis.conf.RedisConfig

import biz.source_code.base64Coder.Base64Coder

import akka.Done

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

class RedisClient(val namespace: String, val pool: Pool, classLoader: ClassLoader)(implicit ec: ExecutionContext){
  private val namespacedKey: String => String =
    x => s"$namespace::$x"

  def destroy(): Unit =
    pool.underlying.destroy()

  def keys: Future[Set[Key]] =
    keys("*")

  def keys(pattern: String): Future[Set[Key]] =
    Future {
      pool.withClient(_.keys(s"$namespace::$pattern"))
    }

  def get[T](key: Key): Future[Option[PortableRedisObject[T]]] =
    try {
      val ttl =
        pool.withJedisClient(_.ttl(namespacedKey(key)).longValue() seconds)

      def valueF =
        Future(pool.withJedisClient(_.get(namespacedKey(key))))

      valueF.map {
        case null =>
          None

        case _data =>
          val data: Seq[String] =
            _data.split("-")

          val bytes =
            Base64Coder.decode(data.last)

          val `object` =
            data.head match {
              case "oos" =>
                withObjectInputStream(bytes)(_.readObject().asInstanceOf[T])

              case "string" =>
                withDataInputStream(bytes)(_.readUTF().asInstanceOf[T])

              case "int" =>
                withDataInputStream(bytes)(_.readInt().asInstanceOf[T])

              case "long" =>
                withDataInputStream(bytes)(_.readLong().asInstanceOf[T])

              case "boolean" =>
                withDataInputStream(bytes)(_.readBoolean().asInstanceOf[T])

              case _ =>
                throw new IOException(
                  s"was not able to recognize the type of serialized value. The type was ${data.head} ")
            }

          Some(PortableRedisObject[T](key, `object`, ttl))
      }
    } catch {
      case _: Exception =>
        Future.successful(None)
    }

  def remove(key: Key): Future[Done] =
    Future {
      pool.withJedisClient(_.del(namespacedKey(key)))
    }.map(_ => Done)

  def set(_key: Key, value: Any, expiration: Duration = Duration.Inf): Future[Done] = {
    val expirationInSec =
      if (expiration == Duration.Inf)
        0
      else
        expiration.toSeconds.toInt

    val key =
      namespacedKey(_key)

    var oos: ObjectOutputStream =
      null

    var dos: DataOutputStream =
      null

    try {
      val baos =
        new ByteArrayOutputStream()

      val prefix =
        value match {
          case _: String =>
            dos =
              new DataOutputStream(baos)

            dos.writeUTF(value.asInstanceOf[String])

            "string"

          case _: Int =>
            dos =
              new DataOutputStream(baos)

            dos.writeInt(value.asInstanceOf[Int])

            "int"

          case _: Long =>
            dos =
              new DataOutputStream(baos)

            dos.writeLong(value.asInstanceOf[Long])

            "long"

          case _: Boolean =>
            dos =
              new DataOutputStream(baos)

            dos.writeBoolean(value.asInstanceOf[Boolean])

            "boolean"

          case _: Serializable =>
            oos =
              new ObjectOutputStream(baos)

            oos.writeObject(value)
            oos.flush()

            "oos"

          case _ =>
            throw new IOException("could not serialize: " + value.toString)
        }

      val redisV =
        prefix + "-" + new String(Base64Coder.encode(baos.toByteArray))

      val valueF =
        Future {
          pool
            .withJedisClient { client =>
              client.set(key, redisV)

              if (expirationInSec != 0)
                client.expire(key, expirationInSec)
            }
          }

      valueF.map { _ =>
        Done
      }
    } finally {
      if (oos != null)
        oos.close()

      if (dos != null)
        dos.close()
    }
  }

  def getOrElseUpdate[A: ClassTag](key: Key, expiration: Duration)(orElse: => Future[PortableRedisObject[A]]): Future[PortableRedisObject[A]] =
    get[A](key)
      .flatMap {
        _.fold {
          orElse
            .map { value =>
              set(key, value, expiration)
              value
            }
        }(Future.successful)
      }

  private class ClassLoaderObjectInputStream(stream: InputStream) extends ObjectInputStream(stream) {
    override protected def resolveClass(desc: ObjectStreamClass): Class[_] = {
      Class.forName(desc.getName, false, classLoader)
    }
  }

  private def withDataInputStream[T](bytes: Array[Byte])(f: DataInputStream => T): T = {
    val dis =
      new DataInputStream(new ByteArrayInputStream(bytes))

    try f(dis) finally dis.close()
  }

  private def withObjectInputStream[T](bytes: Array[Byte])(f: ObjectInputStream => T): T = {
    val ois =
      new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes))

    try f(ois) finally ois.close()
  }
}

object RedisClient {
  def apply(redisConfig: RedisConfig, classLoader: ClassLoader)(implicit ec: ExecutionContext) =
    new RedisClient(redisConfig.namespace, Pool(redisConfig), classLoader)
}
