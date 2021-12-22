package org.byrde.pubsub

import org.byrde.logging.Logger

import java.util.Base64

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.googlecloud.pubsub.{PubSubConfig, PublishMessage, PublishRequest}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.scaladsl.{Flow, Sink, Source}

import io.circe.{Encoder, Printer}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

class Publisher(config: conf.PubSubConfig)(implicit logger: Logger, system: ActorSystem) {
  @nowarn
  private lazy val _config =
    PubSubConfig(config.projectId, config.clientEmail, config.privateKey)
  
  private lazy val _printer: Printer =
    Printer.noSpaces.copy(dropNullValues = true)
  
  def publish[T](env: Envelope[T])(implicit encoder: Encoder[T], ec: ExecutionContext): Future[Unit] = {
    logger.logInfo(s"Publishing message: $env")
    Source
      .single(PublishRequest(Seq(convertMessage(env))))
      .via(flow(env.topic))
      .runWith(Sink.seq)
      .map(_ => ())
      .recoverWith {
      case err =>
        logger.logError(s"Error publishing message: $env", err)
        Future.failed(err)
      }
  }
  
  private def convertMessage[T](env: Envelope[T])(implicit encoder: Encoder[T]): PublishMessage =
    PublishMessage(Base64.getEncoder.encodeToString(env.asJson.printWith(_printer).getBytes))

  private def flow(topic: Topic): Flow[PublishRequest, Seq[String], NotUsed] =
    GooglePubSub.publish(topic, _config)
}
