package org.byrde.jedis.conf

import org.byrde.redis.conf.RedisConfig

import java.net.URI

import com.typesafe.config.Config
import redis.clients.jedis.JedisPoolConfig

import scala.util.Try

trait JedisConfig extends RedisConfig {
  def poolConfig: JedisPoolConfig
}

object JedisConfig {

  def apply(config: Config, namespace: String = "global"): JedisConfig = {
    val redisUri =
      Try(config.getString("redis.uri")).map(new URI(_))

    val _host =
      Try(config.getString("redis.host"))
        .orElse(redisUri.map(_.getHost))
        .getOrElse("localhost")

    val _port =
      Try(config.getInt("redis.port"))
        .orElse(redisUri.map(_.getPort).filter(_ != -1))
        .getOrElse(6379)

    val _password =
      Try(config.getString("redis.password"))
        .orElse {
          redisUri
            .map(_.getUserInfo)
            .filter(_ != null)
            .filter(_ contains ":")
            .map(_.split(":", 2)(1))
        }
        .getOrElse(null)

    val _timeout =
      Try(config.getInt("redis.timeout")).getOrElse(2000)

    val _database =
      Try(config.getInt("redis.database")).getOrElse(0)

    val _poolConfig =
      createPoolConfig(config)

    new JedisConfig {
      override def poolConfig: JedisPoolConfig =
        _poolConfig

      override def host: String =
        _host

      override def port: Int =
        _port

      override def password: String =
        _password

      override def timeout: Int =
        _timeout

      override def database: Int =
        _database
    }
  }

  private def createPoolConfig(config: Config): JedisPoolConfig = {
    val poolConfig: JedisPoolConfig =
      new JedisPoolConfig()

    Try(config.getInt("redis.pool.maxIdle")).foreach(poolConfig.setMaxIdle)
    Try(config.getInt("redis.pool.minIdle")).foreach(poolConfig.setMinIdle)
    Try(config.getInt("redis.pool.maxTotal")).foreach(poolConfig.setMaxTotal)
    Try(config.getLong("redis.pool.maxWaitMillis")).foreach(poolConfig.setMaxWaitMillis)
    Try(config.getBoolean("redis.pool.testOnBorrow")).foreach(poolConfig.setTestOnBorrow)
    Try(config.getBoolean("redis.pool.testOnReturn")).foreach(poolConfig.setTestOnReturn)
    Try(config.getBoolean("redis.pool.testWhileIdle")).foreach(poolConfig.setTestWhileIdle)
    Try(config.getLong("redis.pool.timeBetweenEvictionRunsMillis")).foreach(poolConfig.setTimeBetweenEvictionRunsMillis)
    Try(config.getInt("redis.pool.numTestsPerEvictionRun")).foreach(poolConfig.setNumTestsPerEvictionRun)
    Try(config.getLong("redis.pool.minEvictableIdleTimeMillis")).foreach(poolConfig.setMinEvictableIdleTimeMillis)
    Try(config.getLong("redis.pool.softMinEvictableIdleTimeMillis")).foreach(poolConfig.setSoftMinEvictableIdleTimeMillis)
    Try(config.getBoolean("redis.pool.lifo")).foreach(poolConfig.setLifo)
    Try(config.getBoolean("redis.pool.blockWhenExhausted")).foreach(poolConfig.setBlockWhenExhausted)

    poolConfig
  }

}