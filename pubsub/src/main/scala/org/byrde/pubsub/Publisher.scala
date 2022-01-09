package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider}
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.{TopicAdminClient, TopicAdminSettings}
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage

import org.byrde.logging.Logger
import org.byrde.support.JavaFutureSupport

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._
import scala.util.{Failure, Success, Try}

trait Publisher extends JavaFutureSupport with AutoCloseable {
  private val _publishers: mutable.Map[String, com.google.cloud.pubsub.v1.Publisher] =
    mutable.Map()
  
  def publish[T](
    credentials: Credentials,
    env: Envelope[T]
  )(implicit logger: Logger, encoder: Encoder[T]): Future[Unit] =
    _publishers
      .get(env.topic)
      .map(Success.apply)
      .getOrElse {
        for {
          _ <-
            Try {
              logger.logInfo(s"Creating topic: ${env.topic}")
              TopicAdminClient
                .create {
                  TopicAdminSettings
                    .newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build()
                }
                .createTopic(env.topic)
            }
          publisher <-
            Try {
              logger.logInfo(s"Creating publisher: ${env.topic}")
              com.google.cloud.pubsub.v1.Publisher
                .newBuilder(env.topic)
                .setCredentialsProvider {
                  new CredentialsProvider {
                    override def getCredentials: Credentials = credentials
                  }
                }
                .build
            }
          _ <-
            Try(_publishers.update(env.topic, publisher))
        } yield publisher
      }
      .pipe {
        case Success(publisher) =>
          logger.logInfo(s"Attempting to publish message to PubSub topic ${env.topic}: ${env.msg}")
          publisher
            .publish(PubsubMessage.newBuilder.setData(ByteString.copyFromUtf8(env.asJson.toString)).build)
            .asScala
            .map(_ => ())

        case Failure(exception) =>
          Future.failed(exception)
      }
  
  override def close(): Unit =
    _publishers.values.foreach(_.shutdown())
}

object Publisher extends Publisher