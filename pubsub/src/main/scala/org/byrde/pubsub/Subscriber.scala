package org.byrde.pubsub

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.{MessageReceiver, SubscriptionAdminClient}
import com.google.protobuf.Duration

import org.byrde.logging.Logger

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._
import scala.util.chaining._

trait Subscriber {
  def subscribe[T](
    credentials: Credentials,
    subscription: Subscription,
    topic: Topic
  )(fn: Envelope[T] => Future[_])(implicit logger: Logger, decoder: Decoder[T]): Future[Unit] =
    (for {
      _ <-
        Try {
          logger.logInfo(s"Creating subscription: $subscription")
          SubscriptionAdminClient
            .create()
            .createSubscription {
              com.google.pubsub.v1.Subscription
                .newBuilder()
                .setName(subscription)
                .setTopic(topic)
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
      subscriber <-
        Try {
          logger.logInfo(s"Creating subscriber: $subscription")
          com.google.cloud.pubsub.v1.Subscriber
            .newBuilder(subscription, receiver(fn))
            .setCredentialsProvider {
              new CredentialsProvider {
                override def getCredentials: Credentials = credentials
              }
            }
            .build()
        }
    } yield subscriber).pipe {
      case Success(subscriber) =>
        logger.logInfo(s"Starting subscriber $subscription!")
        for {
          _ <-
            Future.unit
          _ <-
            Future(subscriber.awaitRunning())
              .recoverWith {
                case ex =>
                  logger.logError("Error starting subscriber!", ex)
                  Future(subscriber.awaitTerminated()).flatMap(_ => Future.failed(ex))
              }
        } yield ()

      case Failure(exception) =>
        Future.failed(exception)
    }
  
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