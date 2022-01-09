package org.byrde.pubsub

class PubSubError(throwable: Throwable) extends Throwable(throwable)

object PubSubError {
  case class DecodingError(message: String)(failure: io.circe.Error) extends PubSubError(failure)
  
  case object NoMessage extends PubSubError(new Exception("No Message!"))
}
