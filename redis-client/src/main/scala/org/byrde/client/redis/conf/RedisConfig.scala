package org.byrde.client.redis.conf

trait RedisConfig {

  def host: String

  def port: Int

  def password: String

  def timeout: Int

  def database: Int

}
