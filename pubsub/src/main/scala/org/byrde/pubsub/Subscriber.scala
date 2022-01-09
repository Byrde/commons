package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider}
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1._
import com.google.protobuf.Duration
import com.google.pubsub.v1.{SubscriptionName, TopicName}

import org.byrde.logging.Logger

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._
import scala.util.chaining._

trait Subscriber {
  def createSubscription(
    credentials: Credentials,
    project: String,
    subscription: Subscription,
    topic: Topic
  ): Future[Unit] =
    Future {
      SubscriptionAdminClient
        .create {
          SubscriptionAdminSettings
            .newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
        }
        .createSubscription {
          com.google.pubsub.v1.Subscription
            .newBuilder()
            .setName(SubscriptionName.of(project, subscription).toString)
            .setTopic(TopicName.ofProjectTopicName(project, topic).toString)
            .setAckDeadlineSeconds(10)
            .setMessageRetentionDuration(Duration.newBuilder().setSeconds(604800).build())
            .setExpirationPolicy(com.google.pubsub.v1.ExpirationPolicy.newBuilder().build())
            .setRetryPolicy {
              com.google.pubsub.v1.RetryPolicy
                .newBuilder()
                .setMinimumBackoff(Duration.newBuilder().setSeconds(1).build())
                .setMaximumBackoff(Duration.newBuilder().setSeconds(180).build())
            }
            .build()
        }
    }
    .map(_ => ())
    .recover {
      case _: AlreadyExistsException =>
        Future.unit
    }
  
  def subscribe[T](
    credentials: Credentials,
    project: String,
    subscription: Subscription,
    topic: Topic
  )(fn: Envelope[T] => Future[_])(implicit logger: Logger, decoder: Decoder[T]): Future[Unit] =
    for {
      _ <- createSubscription(credentials, project, subscription, topic)
      subscriber <-
        Future {
          logger.logInfo(s"Creating subscriber: $subscription")
          com.google.cloud.pubsub.v1.Subscriber
            .newBuilder(SubscriptionName.of(project, subscription).toString, receiver(fn))
            .setCredentialsProvider {
              new CredentialsProvider {
                override def getCredentials: Credentials = credentials
              }
            }
            .build()
        }
      _ <-
        Future(subscriber.startAsync().awaitRunning())
          .recoverWith {
            case ex =>
              logger.logError("Error starting subscriber!", ex)
              Future(subscriber.stopAsync().awaitTerminated()).flatMap(_ => Future.failed(ex))
          }
    } yield ()
  
  private def receiver[T](
    fn: Envelope[T] => Future[_],
  )(implicit logger: Logger, decoder: Decoder[T]): MessageReceiver = {
    case (message, consumer) =>
    parse(message.getData.toStringUtf8)
      .flatMap(_.as[Envelope[T]])
      .left
      .map(PubSubError.DecodingError.apply(s"Error decoding message: $message")(_))
      .pipe {
        case Right(value) =>
          fn(value)

        case Left(exception) =>
          Future.failed(exception)
      }
      .map(_ => consumer.ack())
      .recover {
        case ex =>
          logger.logError("Error processing PubSubMessage!", ex)
          consumer.nack()
          ()
      }
  }
}

object Subscriber extends Subscriber