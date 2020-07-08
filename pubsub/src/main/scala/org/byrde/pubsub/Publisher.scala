package org.byrde.pubsub

import java.util.Base64

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.googlecloud.pubsub.{PublishMessage, PublishRequest, PubSubConfig}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}

class Publisher(config: conf.PubSubConfig)(implicit ec: ExecutionContext, system: ActorSystem) {
  
  private val _config = PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  def publish[T](env: Envelope[T])(implicit encoder: Encoder[T]): Future[Unit] =
    Source
      .single(PublishRequest(Seq(convertMessage(env))))
      .via(flow(env.topic))
      .runWith(Sink.seq)
      .map(_ => ())
  
  private def convertMessage[T](env: Envelope[T])(implicit encoder: Encoder[T]): PublishMessage =
    PublishMessage(new String(Base64.getEncoder.encode(env.asJson.toString.getBytes)))

  private def flow(topic: Topic): Flow[PublishRequest, Seq[String], NotUsed] =
    GooglePubSub.publish(topic, _config)
  
}
