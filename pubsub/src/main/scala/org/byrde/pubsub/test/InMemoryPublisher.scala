package org.byrde.pubsub.test

import org.byrde.pubsub._

import io.circe.Encoder

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/** In-memory implementation of MessagePublisher for testing.
  *
  * This implementation stores published messages in memory and provides methods to inspect them for test assertions.
  */
class InMemoryPublisher extends MessagePublisher {
  private val published: TrieMap[String, List[Envelope[Any]]] = TrieMap.empty
  private var shouldFail: Option[PubSubError] = None

  override def publish[T](envelope: Envelope[T])(implicit encoder: Encoder[T]): Future[Either[PubSubError, Unit]] =
    shouldFail match {
      case Some(error) =>
        Future.successful(Left(error))

      case None =>
        published.updateWith(envelope.topic) {
          case Some(envelopes) => Some(envelopes :+ envelope)
          case None => Some(List(envelope))
        }
        Future.successful(Right(()))
    }

  override def close(): Unit = published.clear()

  // Test helpers

  /** Returns all messages published to a specific topic.
    */
  def getPublished(topic: String): List[Envelope[Any]] = published.getOrElse(topic, List.empty)

  /** Returns all messages published to all topics.
    */
  def getAllPublished: Map[String, List[Envelope[Any]]] = published.toMap

  /** Clears all published messages.
    */
  def clear(): Unit = published.clear()

  /** Sets the publisher to fail with the given error on the next publish.
    */
  def setShouldFail(error: PubSubError): Unit = shouldFail = Some(error)

  /** Clears the failure setting.
    */
  def clearFailure(): Unit = shouldFail = None

  /** Returns the number of messages published to a topic.
    */
  def count(topic: String): Int = published.get(topic).map(_.size).getOrElse(0)

  /** Returns the total number of messages published to all topics.
    */
  def totalCount: Int = published.values.map(_.size).sum
}
