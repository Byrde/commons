package org.byrde.client.redis.test

import org.byrde.client.redis.{ BaseRedisClient, RedisService }

import scala.concurrent.ExecutionContext

/** In-memory implementation of RedisClient for testing.
  *
  * Uses InMemoryRedisService for storage.
  */
class InMemoryRedisClient(memoryService: InMemoryRedisService)(implicit ec: ExecutionContext) extends BaseRedisClient {

  override protected def service: RedisService = memoryService
}

object InMemoryRedisClient {

  /** Creates a new InMemoryRedisClient with a fresh InMemoryRedisService.
    */
  def apply()(implicit ec: ExecutionContext): InMemoryRedisClient = new InMemoryRedisClient(new InMemoryRedisService())

  /** Creates a new InMemoryRedisClient with the given InMemoryRedisService.
    */
  def apply(service: InMemoryRedisService)(implicit ec: ExecutionContext): InMemoryRedisClient =
    new InMemoryRedisClient(service)
}
