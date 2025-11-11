package org.byrde.pubsub

/** Base trait for Pub/Sub errors.
  */
sealed trait PubSubError

object PubSubError {

  /** Error during message encoding/decoding.
    */
  case class DecodingError(message: String, failure: io.circe.Error) extends PubSubError

  /** Error during message publishing.
    */
  case class PublishError(message: String, cause: Throwable) extends PubSubError

  /** Error during subscription operations.
    */
  case class SubscriptionError(message: String, cause: Throwable) extends PubSubError

  /** No message available.
    */
  case object NoMessage extends PubSubError

  /** Topic or subscription already exists.
    */
  case class AlreadyExists(resource: String) extends PubSubError

  /** Generic operation error.
    */
  case class OperationError(message: String, cause: Throwable) extends PubSubError
}
