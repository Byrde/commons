package org.byrde.redis.conf

import java.net.URI

import com.typesafe.config.Config
import redis.clients.jedis.JedisPoolConfig

import scala.util.Try

case class RedisConfig(poolConfig: JedisPoolConfig,
                       namespace: String,
                       host: String,
                       port: Int,
                       password: String,
                       timeout: Int,
                       database: Int)

object RedisConfig {
  def apply(config: Config, namespace: String = "global"): RedisConfig = {
    val redisUri =
      Try(config.getString("redis.uri")).map(new URI(_))

    val host =
      Try(config.getString("redis.host"))
        .orElse(redisUri.map(_.getHost))
        .getOrElse("localhost")

    val port =
      Try(config.getInt("redis.port"))
        .orElse(redisUri.map(_.getPort).filter(_ != -1))
        .getOrElse(6379)

    val password =
      Try(config.getString("redis.password"))
        .orElse {
          redisUri
            .map(_.getUserInfo)
            .filter(_ != null)
            .filter(_ contains ":")
            .map(_.split(":", 2)(1))
        }
        .getOrElse(null)

    val timeout =
      Try(config.getInt("redis.timeout")).getOrElse(2000)

    val database =
      Try(config.getInt("redis.database")).getOrElse(0)

    val poolConfig =
      createPoolConfig(config)

    RedisConfig(
      poolConfig,
      namespace,
      host,
      port,
      password,
      timeout,
      database)
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
