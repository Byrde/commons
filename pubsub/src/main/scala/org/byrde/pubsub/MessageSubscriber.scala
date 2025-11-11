package org.byrde.pubsub

import io.circe.Decoder

import scala.concurrent.Future

/** Trait for subscribing to messages from a topic.
  *
  * Implementations handle the underlying infrastructure (Google Pub/Sub, Kafka, in-memory, etc.)
  */
trait MessageSubscriber {

  /** Subscribes to messages from a topic.
    *
    * @param subscription
    *   The subscription name
    * @param topic
    *   The topic name
    * @param handler
    *   Function to handle received messages, returning Ack or Nack
    * @param decoder
    *   Implicit decoder for the message type
    * @tparam T
    *   The message type
    * @return
    *   Future with Either containing PubSubError on failure or Unit on success
    */
  def subscribe[T](
    subscription: String,
    topic: String,
  )(
    handler: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Either[PubSubError, Unit]]

  /** Closes the subscriber and releases all resources.
    */
  def close(): Unit
}
