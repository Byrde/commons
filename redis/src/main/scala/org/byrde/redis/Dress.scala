package org.byrde.redis

import java.lang

import redis.clients.jedis.{Jedis, SortingParams}

import scala.concurrent.duration.FiniteDuration

trait Dress {
  implicit def delegateToJedis(d: Wrap): Jedis =
    d.j

  implicit def fromJedistoScala(j: Jedis): Wrap =
    up(j)

  class Wrap(val j: Jedis) {
    import scala.jdk.CollectionConverters._

    def keys(pattern: String): Set[String] =
      j.keys(pattern).asScala.toSet

    def expire(key: String, time: FiniteDuration): lang.Long =
      j.expire(key, time.toSeconds.toInt)

    def hmset(key: String, values: Map[String, String]): String =
      j.hmset(key,values.asJava)

    def hmget(key: String, values: String*): List[Option[String]] =
      j.hmget(key,values: _*).asScala.toList.map(Option.apply)

    def hget(key: String, field: String): Option[String] =
      Option(j.hget(key, field))

    def hgetAll(key: String): Map[String,String] =
      j.hgetAll(key).asScala.toMap

    def smembers(key: String):Set[String] =
      j.smembers(key).asScala.toSet

    def sinter(key: String): Set[String] =
      j.sinter(key).asScala.toSet

    def sunion(key: String): Set[String] =
      j.sunion(key).asScala.toSet

    def sdiff(key: String): Set[String] =
      j.sdiff(key).asScala.toSet

    def zrange(key: String, start: Long, end: Long): Set[String] =
      j.zrange(key, start, end).asScala.toSet

    def hkeys(key: String): Set[String] =
      j.hkeys(key).asScala.toSet

    def hvals(key: String): List[String] =
      j.hvals(key).asScala.toList

    def get(k: String): Option[String] =
      Option(j.get(k))

    def lrange(key: String, start: Long, end: Long): List[String] =
      j.lrange(key,start,end).asScala.toList

    def sort(key: String, params: SortingParams): List[String] =
      j.sort(key,params).asScala.toList

    def sort(key: String):List[String] =
      j.sort(key).asScala.toList

    def blpop(timeout: Int, args: String*): List[String] =
      j.blpop(timeout, args:_*).asScala.toList

    def blpop(args: String*): List[String] =
      j.blpop(args:_*).asScala.toList

    def brpop(timeout: Int, args: String*): List[String] =
      j.brpop(timeout, args:_*).asScala.toList

    def brpop(args: String*): List[String] =
      j.brpop(args:_*).asScala.toList
  }

  def up(j: Jedis) =
    new Wrap(j)
}

object Dress extends Dress