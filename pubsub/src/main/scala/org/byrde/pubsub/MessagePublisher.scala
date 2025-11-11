package org.byrde.pubsub

import io.circe.Encoder

import scala.concurrent.Future

/** Trait for publishing messages to a topic.
  *
  * Implementations handle the underlying infrastructure (Google Pub/Sub, Kafka, in-memory, etc.)
  */
trait MessagePublisher {

  /** Publishes a message to a topic.
    *
    * @param envelope
    *   The message envelope containing the message and metadata
    * @param encoder
    *   Implicit encoder for the message type
    * @tparam T
    *   The message type
    * @return
    *   Future with Either containing PubSubError on failure or Unit on success
    */
  def publish[T](envelope: Envelope[T])(implicit encoder: Encoder[T]): Future[Either[PubSubError, Unit]]

  /** Closes the publisher and releases all resources.
    */
  def close(): Unit
}
