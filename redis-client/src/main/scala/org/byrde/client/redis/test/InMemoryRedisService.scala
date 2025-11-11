package org.byrde.client.redis.test

import org.byrde.client.redis.{ Key, RedisClientError, RedisService }

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }

/** In-memory implementation of RedisService for testing.
  *
  * This implementation stores key-value pairs in memory and provides methods to inspect them for test assertions.
  */
class InMemoryRedisService(implicit ec: ExecutionContext) extends RedisService {

  private case class StoredValue(value: String, expiresAt: Option[Long])

  private val store: TrieMap[Key, StoredValue] = TrieMap.empty
  private var shouldFail: Option[RedisClientError] = None

  override def keys(pattern: String): Future[Either[RedisClientError, Set[String]]] =
    Future {
      shouldFail match {
        case Some(error) => Left(error)
        case None =>
          cleanup()
          val regex = pattern.replace("*", ".*").r
          Right(store.keys.filter(k => regex.matches(k)).toSet)
      }
    }

  override def get(key: Key): Future[Either[RedisClientError, Option[String]]] =
    Future {
      shouldFail match {
        case Some(error) => Left(error)
        case None =>
          cleanup()
          store.get(key) match {
            case Some(stored) if !isExpired(stored) => Right(Some(stored.value))
            case Some(_) =>
              store.remove(key)
              Right(None)
            case None => Right(None)
          }
      }
    }

  override def set(key: Key, value: String, expiration: Duration): Future[Either[RedisClientError, Unit]] =
    Future {
      shouldFail match {
        case Some(error) => Left(error)
        case None =>
          val expiresAt =
            if (expiration == Duration.Inf) None else Some(System.currentTimeMillis() + expiration.toMillis)
          store.put(key, StoredValue(value, expiresAt))
          Right(())
      }
    }

  override def del(key: Key): Future[Either[RedisClientError, Long]] =
    Future {
      shouldFail match {
        case Some(error) => Left(error)
        case None =>
          val removed = if (store.remove(key).isDefined) 1L else 0L
          Right(removed)
      }
    }

  override def ttl(key: Key): Future[Either[RedisClientError, Long]] =
    Future {
      shouldFail match {
        case Some(error) => Left(error)
        case None =>
          cleanup()
          store.get(key) match {
            case Some(stored) if !isExpired(stored) =>
              stored.expiresAt match {
                case Some(expiresAt) =>
                  val ttlMillis = expiresAt - System.currentTimeMillis()
                  Right(ttlMillis / 1000) // Convert to seconds
                case None => Right(-1L) // No expiration
              }
            case Some(_) =>
              store.remove(key)
              Right(-2L) // Key doesn't exist
            case None => Right(-2L) // Key doesn't exist
          }
      }
    }

  // Test helpers

  /** Clears all stored keys.
    */
  def clear(): Unit = store.clear()

  /** Returns all stored keys.
    */
  def getAllKeys: Set[String] = {
    cleanup()
    store.keys.toSet
  }

  /** Returns the number of stored keys.
    */
  def size: Int = {
    cleanup()
    store.size
  }

  /** Sets the service to fail with the given error on the next operation.
    */
  def setShouldFail(error: RedisClientError): Unit = shouldFail = Some(error)

  /** Clears the failure setting.
    */
  def clearFailure(): Unit = shouldFail = None

  /** Resets the service to initial state.
    */
  def reset(): Unit = {
    clear()
    clearFailure()
  }

  // Private helpers

  private def isExpired(stored: StoredValue): Boolean = stored.expiresAt.exists(_ < System.currentTimeMillis())

  private def cleanup(): Unit = store.filterInPlace((_, stored) => !isExpired(stored))
}
