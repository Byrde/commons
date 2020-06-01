package org.byrde.pubsub

import akka.Done
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.alpakka.googlecloud.pubsub.{AcknowledgeRequest, PubSubConfig, ReceivedMessage}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration._

class Subscriber[T](
  subscription: Subscription,
  handler: MessageHandler[T]
)(config: conf.PubSubConfig)(implicit system: ActorSystem) {
  
  private val _config = PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  private val subscriptionSource: Source[ReceivedMessage, Cancellable] =
    GooglePubSub.subscribe(subscription, _config)
  
  private val ackSink: Sink[AcknowledgeRequest, Future[Done]] =
    GooglePubSub.acknowledge(subscription, _config)
  
  subscriptionSource
    .mapAsync(config.batch)(handler.process)
    .groupedWithin(config.batch, 1.seconds)
    .map(AcknowledgeRequest.apply)
    .to(ackSink)
  
}
