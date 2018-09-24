package org.byrde.commons.utils.redis.conf

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

import java.net.URI

import org.sedis.Pool

import play.api.Configuration

case class RedisConfig(poolConfig: JedisPoolConfig,
                       namespace: String,
                       host: String,
                       port: Int,
                       password: String,
                       timeout: Int,
                       database: Int)

object RedisConfig {
  def apply(config: Configuration, namespace: String = "global"): RedisConfig = {
    val redisUri =
      config
        .getOptional[String]("redis.uri")
        .map(new URI(_))

    val host =
      config
        .getOptional[String]("redis.host")
        .orElse(redisUri.map(_.getHost))
        .getOrElse("localhost")

    val port =
      config
        .getOptional[Int]("redis.port")
        .orElse(redisUri.map(_.getPort).filter(_ != -1))
        .getOrElse(6379)

    val password =
      config
        .getOptional[String]("redis.password")
        .orElse {
          redisUri
            .map(_.getUserInfo)
            .filter(_ != null)
            .filter(_ contains ":")
            .map(_.split(":", 2)(1))
        }
        .orNull

    val timeout =
      config
        .getOptional[Int]("redis.timeout")
        .getOrElse(2000)

    val database =
      config
        .getOptional[Int]("redis.database")
        .getOrElse(0)

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

  private def createPoolConfig(config: Configuration): JedisPoolConfig = {
    val poolConfig: JedisPoolConfig =
      new JedisPoolConfig()

    config.getOptional[Int]("redis.pool.maxIdle").foreach(poolConfig.setMaxIdle)
    config.getOptional[Int]("redis.pool.minIdle").foreach(poolConfig.setMinIdle)
    config.getOptional[Int]("redis.pool.maxTotal").foreach(poolConfig.setMaxTotal)
    config.getOptional[Long]("redis.pool.maxWaitMillis").foreach(poolConfig.setMaxWaitMillis)
    config.getOptional[Boolean]("redis.pool.testOnBorrow").foreach(poolConfig.setTestOnBorrow)
    config.getOptional[Boolean]("redis.pool.testOnReturn").foreach(poolConfig.setTestOnReturn)
    config.getOptional[Boolean]("redis.pool.testWhileIdle").foreach(poolConfig.setTestWhileIdle)
    config.getOptional[Long]("redis.pool.timeBetweenEvictionRunsMillis").foreach(poolConfig.setTimeBetweenEvictionRunsMillis)
    config.getOptional[Int]("redis.pool.numTestsPerEvictionRun").foreach(poolConfig.setNumTestsPerEvictionRun)
    config.getOptional[Long]("redis.pool.minEvictableIdleTimeMillis").foreach(poolConfig.setMinEvictableIdleTimeMillis)
    config.getOptional[Long]("redis.pool.softMinEvictableIdleTimeMillis").foreach(poolConfig.setSoftMinEvictableIdleTimeMillis)
    config.getOptional[Boolean]("redis.pool.lifo").foreach(poolConfig.setLifo)
    config.getOptional[Boolean]("redis.pool.blockWhenExhausted").foreach(poolConfig.setBlockWhenExhausted)

    poolConfig
  }
}
