package org.byrde.pubsub.test

import org.byrde.pubsub._

import io.circe.Decoder

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/** In-memory implementation of MessageSubscriber for testing.
  *
  * This implementation allows you to simulate message delivery for testing without requiring actual Pub/Sub
  * infrastructure.
  */
class InMemorySubscriber extends MessageSubscriber {
  private val handlers: TrieMap[String, Envelope[Any] => Future[Either[Nack.type, Ack.type]]] = TrieMap.empty
  private var shouldFail: Option[PubSubError] = None

  override def subscribe[T](
    subscription: String,
    topic: String,
  )(
    handler: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Either[PubSubError, Unit]] =
    shouldFail match {
      case Some(error) =>
        Future.successful(Left(error))

      case None =>
        handlers.put(subscription, handler.asInstanceOf[Envelope[Any] => Future[Either[Nack.type, Ack.type]]])
        Future.successful(Right(()))
    }

  override def close(): Unit = handlers.clear()

  // Test helpers

  /** Simulates delivery of a message to a subscription.
    *
    * Returns the result of the handler (Ack or Nack), or None if no handler is registered for the subscription.
    */
  def simulateMessage[T](
    subscription: String,
    envelope: Envelope[T],
  ): Option[Future[Either[Nack.type, Ack.type]]] =
    handlers.get(subscription).map(_(envelope.asInstanceOf[Envelope[Any]]))

  /** Checks if a subscription has a registered handler.
    */
  def hasSubscription(subscription: String): Boolean = handlers.contains(subscription)

  /** Returns all active subscriptions.
    */
  def getSubscriptions: Set[String] = handlers.keySet.toSet

  /** Clears all subscriptions.
    */
  def clear(): Unit = handlers.clear()

  /** Sets the subscriber to fail with the given error on the next subscribe.
    */
  def setShouldFail(error: PubSubError): Unit = shouldFail = Some(error)

  /** Clears the failure setting.
    */
  def clearFailure(): Unit = shouldFail = None
}
