package org.byrde.pubsub

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.alpakka.googlecloud.pubsub.{PubSubConfig, PublishMessage, PublishRequest}
import akka.stream.scaladsl.{Flow, Sink, Source}

import java.util.Base64

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

import zio.{Task, ZIO}

class Publisher(config: conf.PubSubConfig)(implicit system: ActorSystem) {
  
  private val _config = PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  def publish[T: Encoder](message: Message[T]): Task[Unit] =
    ZIO.fromFuture { _ =>
      Source
        .single(PublishRequest(Seq(convertMessage(message))))
        .via(flow(message.topic))
        .runWith(Sink.seq)
    }.map(_ => ())
  
  private def convertMessage[T: Encoder](message: Message[T]): PublishMessage =
    PublishMessage(new String(Base64.getEncoder.encode(message.asJson.toString.getBytes)))

  private def flow(topic: Topic): Flow[PublishRequest, Seq[String], NotUsed] =
    GooglePubSub.publish(topic, _config)
  
}
