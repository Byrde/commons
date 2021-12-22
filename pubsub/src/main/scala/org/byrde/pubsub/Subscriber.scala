package org.byrde.pubsub

import org.byrde.logging.Logger

import java.util.Base64

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._

import akka.actor.ActorSystem
import akka.stream.RestartSettings
import akka.stream.alpakka.googlecloud.pubsub._
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.scaladsl.RestartSource

import scala.annotation.nowarn
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class Subscriber(
  config: conf.PubSubConfig,
)(implicit logger: Logger, system: ActorSystem) {
  @nowarn
  private val _config: PubSubConfig =
    PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  def process[T](
    subscription: Subscription,
    fn: Envelope[T] => Future[Unit]
  )(
    message: ReceivedMessage
  )(implicit decoder: Decoder[T], ec: ExecutionContext): Future[MessageId] =
    message
      .message
      .data
      .map(convertMessage(_))
      .map {
        case Right(innerMessage) =>
          logger.logInfo(s"Processing message for subscription: $subscription, $innerMessage")
          fn(innerMessage).map(_ => message.ackId)

        case Left(err) =>
          Future.failed(err)
      }
      .getOrElse(Future.failed(PubSubError.NoMessage))
  
  def start[T](
    subscription: Subscription
  )(
    fn: Envelope[T] => Future[Unit]
  )(implicit decoder: Decoder[T], ec: ExecutionContext): Unit =
    RestartSource
      .withBackoff(
        RestartSettings(1.second, 10.seconds, 0.1)
      )(() => GooglePubSub.subscribe(subscription, _config))
      .mapAsync(config.batch) { message =>
        process(subscription, fn)(message).recoverWith {
          case err =>
            logger.logError(s"Error processing message for subscription: $subscription, $message", err)
            Future.failed(err)
        }
      }
      .groupedWithin(config.batch, 1.second)
      .map(AcknowledgeRequest.apply)
      .to(GooglePubSub.acknowledge(subscription, _config))
      .run()

  private def decode(message: String) =
    parse(new String(Base64.getDecoder.decode(message)))

  private def convertMessage[T](message: String)(implicit decoder: Decoder[T]): Either[PubSubError, Envelope[T]] =
    decode(message)
      .left
      .map(PubSubError.ParsingError(message))
      .flatMap(_.as[Envelope[T]].left.map(PubSubError.DecodingError(message)))
}
