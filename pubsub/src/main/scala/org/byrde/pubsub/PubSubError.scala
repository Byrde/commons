package org.byrde.pubsub

import io.circe.{DecodingFailure, ParsingFailure}

class PubSubError(throwable: Throwable) extends Throwable(throwable)

object PubSubError {
  case class ParsingError(message: String)(failure: ParsingFailure) extends PubSubError(failure)
  
  case class DecodingError(message: String)(failure: DecodingFailure) extends PubSubError(failure)
  
  case object NoMessage extends PubSubError(new Exception("No Message!"))
}
