package org.byrde.pubsub

import akka.stream.alpakka.googlecloud.pubsub.ReceivedMessage

import org.byrde.logging.{Logger, Logging}

import java.util.Base64

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._

import zio._

import scala.concurrent.Future

abstract class MessageHandler[T: Decoder](runtime: Runtime[Unit])(implicit logger: Logger) extends Logging {
  
  def handle(message: Message[T]): IO[PubSubError, Unit]
  
  private def decode(message: String) =
    new String(Base64.getDecoder.decode(message))
  
  private def convertMessage(message: String): IO[PubSubError, Message[T]] =
    decode(message).asJson.as[Message[T]] match {
      case Right(message) =>
        ZIO.succeed(message)
      
      case Left(failure) =>
        error(s"Failed to decode message: $message", failure).provide(logger)
        ZIO.fail(PubSubError.DecodingError(message)(failure))
    }
  
  def process(message: ReceivedMessage): Future[String] =
    runtime.unsafeRun {
      message.message.data
        .map(convertMessage)
        .map(_.flatMap(handle))
        .map(_.map(_ => message.ackId))
        .getOrElse(ZIO.fail(PubSubError.NoMessage))
        .toFuture
    }.future
  
}
