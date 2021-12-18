package org.byrde.pubsub

import org.byrde.logging.Logger

import java.util.Base64

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._

import akka.Done
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.alpakka.googlecloud.pubsub._
import akka.stream.scaladsl.{RestartSource, Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

abstract class Subscriber[T](
  subscription: Subscription,
  config: conf.PubSubConfig,
  logger: Logger
)(implicit ec: ExecutionContext, system: ActorSystem, decoder: Decoder[T]) {
  lazy val _config: PubSubConfig =
    PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  def _subscriptionSource: Source[ReceivedMessage, Cancellable] =
    GooglePubSub.subscribe(subscription, _config)
  
  private val _ackSink: Sink[AcknowledgeRequest, Future[Done]] =
    GooglePubSub.acknowledge(subscription, _config)

  def handle(env: Envelope[T]): Future[Unit]

  def process(message: ReceivedMessage): Future[MessageId] =
    message.message.data
      .map(convertMessage)
      .map {
        case Right(innerMessage) =>
          logger.logInfo(s"Handling message for subscription: $subscription")
          handle(innerMessage).map(_ => message.ackId)

        case Left(err) =>
          Future.failed(err)
      }
      .getOrElse(Future.failed(PubSubError.NoMessage))
  
  def start(): Unit =
    RestartSource.withBackoff(1.second, 10.seconds, 0.1)(() => _subscriptionSource)
      .mapAsync(config.batch) { message =>
        process(message).recoverWith {
          case err =>
            logger.logError(s"Error processing message for subscription: $subscription", err)
            Future.failed(err)
        }
      }
      .groupedWithin(config.batch, 1.second)
      .map(AcknowledgeRequest.apply)
      .to(_ackSink)
      .run()

  private def decode(message: String) =
    parse(new String(Base64.getDecoder.decode(message)))

  private def convertMessage(message: String): Either[PubSubError, Envelope[T]] =
    decode(message)
      .left
      .map(PubSubError.ParsingError(message))
      .flatMap(_.as[Envelope[T]].left.map(PubSubError.DecodingError(message)))
}
