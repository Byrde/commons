package org.byrde.pubsub

import java.util.Base64

import org.byrde.logging.{Logger, Logging}

import io.circe.Decoder

import akka.Done
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.alpakka.googlecloud.pubsub.{AcknowledgeRequest, PubSubConfig, ReceivedMessage}
import akka.stream.scaladsl.{Sink, Source}

import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

abstract class Subscriber[T](
  subscription: Subscription,
)(
  config: conf.PubSubConfig
)(implicit ec: ExecutionContext, logger: Logger, system: ActorSystem, decoder: Decoder[T])
  extends Logging {
  
  private val _config = PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  private val _subscriptionSource: Source[ReceivedMessage, Cancellable] =
    GooglePubSub.subscribe(subscription, _config)
  
  private val _ackSink: Sink[AcknowledgeRequest, Future[Done]] =
    GooglePubSub.acknowledge(subscription, _config)

  def handle(env: Envelope[T]): Future[Unit]

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

  private def decode(message: String) =
    new String(Base64.getDecoder.decode(message))

  private def convertMessage(message: String): Either[PubSubError, Envelope[T]] =
    decode(message).asJson.as[Envelope[T]].left.map { failure =>
      error(s"Failed to decode message: $message", failure).provide(logger)
      PubSubError.DecodingError(message)(failure)
    }
  
  _subscriptionSource
    .mapAsync(config.batch)(process)
    .groupedWithin(config.batch, 1.seconds)
    .map(AcknowledgeRequest.apply)
    .to(_ackSink)
  
}
