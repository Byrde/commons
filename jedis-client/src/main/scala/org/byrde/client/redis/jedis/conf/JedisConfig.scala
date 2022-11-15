package org.byrde.client.redis.jedis.conf

import org.byrde.client.redis.conf.RedisConfig

import java.net.URI

import com.typesafe.config.Config

import redis.clients.jedis.JedisPoolConfig

import scala.util.Try

case class JedisConfig(
  host: String,
  port: Int,
  password: String,
  timeout: Int,
  database: Int,
  poolConfig: JedisPoolConfig
) extends RedisConfig

object JedisConfig {
  /**
   * e.g configuration:
   * {
   *   uri: "redis://u:password@localhost:6379"
   *   host: "localhost"
   *   port: 6379
   *   password: "password"
   *   timeout: 2000
   *   database: 0
   * }
   * 
   * @param config - Typesafe config adhering to above example.
   * @return
   */
  def apply(config: Config): JedisConfig = {
    val redisUri =
      Try(config.getString("uri")).map(new URI(_))

    val _host =
      Try(config.getString("host"))
        .orElse(redisUri.map(_.getHost))
        .getOrElse("localhost")

    val _port =
      Try(config.getInt("port"))
        .orElse(redisUri.map(_.getPort).filter(_ != -1))
        .getOrElse(6379)

    val _password =
      Try(config.getString("password"))
        .orElse {
          redisUri
            .map(_.getUserInfo)
            .filter(_ != null)
            .filter(_ contains ":")
            .map(_.split(":", 2)(1))
        }
        .getOrElse(null)

    val _timeout =
      Try(config.getInt("timeout")).getOrElse(2000)

    val _database =
      Try(config.getInt("database")).getOrElse(0)

    val _poolConfig =
      createPoolConfig(config)

    new JedisConfig (
      _host,
      _port,
      _password,
      _timeout,
      _database,
      _poolConfig
    )
  }

  private def createPoolConfig(config: Config): JedisPoolConfig = {
    val poolConfig: JedisPoolConfig =
      new JedisPoolConfig()

    Try(config.getInt("pool.maxIdle")).foreach(poolConfig.setMaxIdle)
    Try(config.getInt("pool.minIdle")).foreach(poolConfig.setMinIdle)
    Try(config.getInt("pool.maxTotal")).foreach(poolConfig.setMaxTotal)
    Try(config.getDuration("pool.maxWaitMillis")).foreach(poolConfig.setMaxWait)
    Try(config.getBoolean("pool.testOnBorrow")).foreach(poolConfig.setTestOnBorrow)
    Try(config.getBoolean("pool.testOnReturn")).foreach(poolConfig.setTestOnReturn)
    Try(config.getBoolean("pool.testWhileIdle")).foreach(poolConfig.setTestWhileIdle)
    Try(config.getDuration("pool.timeBetweenEvictionRunsMillis")).foreach(poolConfig.setTimeBetweenEvictionRuns)
    Try(config.getInt("pool.numTestsPerEvictionRun")).foreach(poolConfig.setNumTestsPerEvictionRun)
    Try(config.getDuration("pool.minEvictableIdleTimeMillis")).foreach(poolConfig.setSoftMinEvictableIdleTime)
    Try(config.getBoolean("pool.lifo")).foreach(poolConfig.setLifo)
    Try(config.getBoolean("pool.blockWhenExhausted")).foreach(poolConfig.setBlockWhenExhausted)

    poolConfig
  }
}