package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider}
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.{TopicAdminClient, TopicAdminSettings}
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{PubsubMessage, TopicName}

import org.byrde.logging.Logger
import org.byrde.support.JavaFutureSupport

import java.util.concurrent.TimeUnit

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._

trait Publisher extends JavaFutureSupport {
  def createTopic(
    credentials: Credentials,
    project: String,
    topic: String
  )(implicit logger: Logger): Future[Unit] =
    Future {
      logger.logInfo(s"Creating topic: $topic")
      TopicAdminClient
        .create {
          TopicAdminSettings
            .newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
        }
        .createTopic(TopicName.ofProjectTopicName(project, topic).toString)
    }
    .map(_ => ())
    .recover {
      case _: AlreadyExistsException =>
        Future.unit
    }
  
  def publish[T](
    credentials: Credentials,
    project: String,
    env: Envelope[T]
  )(implicit logger: Logger, encoder: Encoder[T]): Future[Unit] =
    for {
      _ <- createTopic(credentials, project, env.topic)
      publisher <-
        Future {
          logger.logInfo(s"Creating publisher: ${env.topic}")
          com.google.cloud.pubsub.v1.Publisher
            .newBuilder(TopicName.ofProjectTopicName(project, env.topic).toString)
            .setCredentialsProvider {
              new CredentialsProvider {
                override def getCredentials: Credentials = credentials
              }
            }
            .build
        }
      _ <- {
        logger.logInfo(s"Attempting to publish message to PubSub topic ${env.topic}: ${env.msg}")
        publisher
          .publish(PubsubMessage.newBuilder.setData(ByteString.copyFromUtf8(env.asJson.toString)).build)
          .asScala
      }
      _ <-
        Future(publisher.tap(_.shutdown()).awaitTermination(10, TimeUnit.SECONDS))
    } yield ()
}

object Publisher extends Publisher