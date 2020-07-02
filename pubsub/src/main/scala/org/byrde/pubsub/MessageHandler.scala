package org.byrde.pubsub

import akka.stream.alpakka.googlecloud.pubsub.ReceivedMessage
import org.byrde.logging.{Logger, Logging}
import java.util.Base64

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.ChainingSyntax

abstract class MessageHandler[T: Decoder](implicit ec: ExecutionContext, logger: Logger) extends Logging with ChainingSyntax {
  
  def handle(message: Message[T]): Future[Either[PubSubError, Unit]]
  
  private def decode(message: String) =
    new String(Base64.getDecoder.decode(message))
  
  private def convertMessage(message: String): Either[PubSubError, Message[T]] =
    decode(message).asJson.as[Message[T]].left.map { failure =>
      error(s"Failed to decode message: $message", failure).provide(logger)
      PubSubError.DecodingError(message)(failure)
    }
  
  def process(message: ReceivedMessage): Future[MessageId] =
    message.message.data
      .map(convertMessage)
      .map {
        case Right(innerMessage) =>
          handle(innerMessage).map(_ => message.ackId)

        case Left(err) =>
          Future.failed(err)
      }
      .getOrElse(Future.failed(PubSubError.NoMessage))

}
