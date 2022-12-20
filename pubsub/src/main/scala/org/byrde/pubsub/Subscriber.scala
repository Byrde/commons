package org.byrde.pubsub

import org.byrde.logging.{ Log, Logger }

import com.google.api.core.ApiService
import com.google.api.gax.core.{ CredentialsProvider, FixedCredentialsProvider, NoCredentialsProvider }
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{ AlreadyExistsException, FixedTransportChannelProvider }
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.protobuf.Duration
import com.google.pubsub.v1.{ SubscriptionName, TopicName }

import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{ Decoder, Json }
import io.grpc.ManagedChannelBuilder

import java.util.concurrent.TimeUnit

import scala.annotation.unused
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util._
import scala.util.chaining._

abstract class Subscriber(logger: Logger)(implicit ec: ExecutionContextExecutor)
  extends AdminClient
    with AutoCloseable {

  private type Subscription = String

  private val _awaitTermination = 10 // seconds

  private val _subscribers: scala.collection.concurrent.Map[Subscription, com.google.cloud.pubsub.v1.Subscriber] =
    TrieMap.empty

  def createSubscription(
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean = false,
    enableMessageOrdering: Boolean = false,
    hostOpt: Option[String] = Option.empty,
  ): Unit =
    _createSubscriptionAdminClient(FixedCredentialsProvider.create(credentials), hostOpt).pipe { client =>
      Try {
        client
          .tap(_.createSubscription {
            com
              .google
              .pubsub
              .v1
              .Subscription
              .newBuilder()
              .setName(SubscriptionName.of(project, subscription).toString)
              .setTopic(TopicName.ofProjectTopicName(project, topic).toString)
              // Ack deadline is a reasonable 10 seconds
              .setAckDeadlineSeconds(10)
              // Max retention for unacknowledged messages is 7 days
              .setMessageRetentionDuration(Duration.newBuilder().setSeconds(604800).build())
              // Never expire
              .setExpirationPolicy(com.google.pubsub.v1.ExpirationPolicy.newBuilder().build())
              .setEnableExactlyOnceDelivery(enableExactlyOnceDelivery)
              .setEnableMessageOrdering(enableMessageOrdering)
              .setRetryPolicy {
                com
                  .google
                  .pubsub
                  .v1
                  .RetryPolicy
                  .newBuilder()
                  // Min backoff is 1 second
                  .setMinimumBackoff(Duration.newBuilder().setSeconds(1).build())
                  // Max backoff is 10 minutes
                  .setMaximumBackoff(Duration.newBuilder().setSeconds(600).build())
              }
              .build()
          })
          .tap(_.shutdown())
          .awaitTermination(_awaitTermination, TimeUnit.SECONDS)
        logger.logInfo(s"Created subscription: $subscription")
        ()
      }.recover {
        case _: AlreadyExistsException =>
          logger.logDebug(s"Subscription already exists: $subscription")
          client.tap(_.shutdown()).awaitTermination(_awaitTermination, TimeUnit.SECONDS)
          ()

        case ex =>
          client.tap(_.shutdown()).awaitTermination(_awaitTermination, TimeUnit.SECONDS)
          throw ex
      }
    }

  def subscribe[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean = false,
    enableMessageOrdering: Boolean = false,
    @unused hostOpt: Option[String] = Option.empty,
  )(
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Unit] =
    _subscribers
      .getOrElseUpdate(
        subscription,
        createSubscriptionAndSubscriber(
          credentials,
          project,
          subscription,
          topic,
          enableExactlyOnceDelivery,
          enableMessageOrdering,
          hostOpt,
        )(fn),
      )
      .pipe { subscriber =>
        if (!subscriber.isRunning)
          Future(subscriber.startAsync().awaitRunning())
            .map { _ =>
              logger.logDebug(s"Subscriber started successfully!")
              ()
            }
            .recoverWith {
              case ex =>
                logger.logError("Failed to start the subscriber!", ex)
                closeSubscriber(subscription)
                Future.failed(ex)
            }
        else
          Future.successful(())
      }

  override def close(): Unit = {
    logger.logInfo("Shutting down all subscribers...")
    _subscribers.foreach {
      case (subscription, subscriber) =>
        subscriber.stopAsync().awaitTerminated()
        _subscribers.remove(subscription)
    }
  }

  private def closeSubscriber(subscription: String): Unit =
    _subscribers
      .get(subscription)
      .foreach { subscriber =>
        logger.logInfo(s"Shutting down subscriber: $subscription")
        subscriber.stopAsync().awaitTerminated()
        _subscribers.remove(subscription)
      }

  private def createSubscriptionAndSubscriber[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean,
    enableMessageOrdering: Boolean,
    hostOpt: Option[String],
  )(
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]) = {
    createSubscription(
      credentials,
      project,
      subscription,
      topic,
      enableExactlyOnceDelivery,
      enableMessageOrdering,
      hostOpt,
    )
    logger.logInfo(s"Creating subscriber: $subscription")
    subscriber[T](
      credentials,
      project,
      subscription,
      topic,
      enableExactlyOnceDelivery,
      enableMessageOrdering,
      hostOpt,
    )(fn)
  }

  private def subscriber[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean,
    enableMessageOrdering: Boolean,
    hostOpt: Option[String],
  )(
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): com.google.cloud.pubsub.v1.Subscriber =
    com
      .google
      .cloud
      .pubsub
      .v1
      .Subscriber
      .newBuilder(SubscriptionName.of(project, subscription).toString, receiver(subscription, topic, fn))
      .tap { builder =>
        hostOpt match {
          case None =>
            builder.setCredentialsProvider {
              new CredentialsProvider {
                override def getCredentials: Credentials = credentials
              }
            }

          case Some(host) =>
            val channel = ManagedChannelBuilder.forTarget(host).usePlaintext.build()
            builder
              .setChannelProvider(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
              .setCredentialsProvider(NoCredentialsProvider.create())
        }
      }
      .build()
      .tap { subscriber =>
        subscriber.addListener(
          TerminalFailureApiServiceListener(
            subscription,
            () =>
              subscribe(
                credentials,
                project,
                subscription,
                topic,
                enableExactlyOnceDelivery,
                enableMessageOrdering,
                hostOpt,
              )(fn),
          ),
          ec,
        )
      }

  private def receiver[T](
    subscription: String,
    topic: String,
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): MessageReceiver = {
    case (message, consumer) =>
      message
        .getData
        .toStringUtf8
        .pipe { data =>
          parse(data)
            .flatMap(_.as[Envelope[Json]])
            .left
            .map(PubSubError.DecodingError.apply(s"Error decoding envelope: ${message.getData.toStringUtf8}")(_))
            .pipe {
              case Right(env) =>
                env
                  .msg
                  .as[T]
                  .left
                  .map(PubSubError.DecodingError(s"Error decoding message: ${message.getData.toStringUtf8}")(_))
                  .pipe {
                    case Right(value) =>
                      fn(Envelope(env.topic, value, env.id, env.correlationId))
                        .map {
                          case Right(_) =>
                            logger.logDebug(
                              "Message acknowledged!",
                              Log(
                                "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                                "topic" -> topic,
                                "subscription" -> subscription,
                                "id" -> env.id,
                              ).!++("payload" -> message.getData.toStringUtf8),
                            )
                            consumer.ack()
                            ()

                          case Left(_) =>
                            logger.logDebug(
                              "Message not acknowledged!",
                              Log(
                                "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                                "topic" -> topic,
                                "subscription" -> subscription,
                                "id" -> env.id,
                              ).!++("payload" -> message.getData.toStringUtf8),
                            )
                            consumer.nack()
                            ()
                        }
                        .recover {
                          case ex =>
                            logger.logError(
                              s"Error in the receiver function!",
                              ex,
                              Log(
                                "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                                "topic" -> topic,
                                "subscription" -> subscription,
                                "id" -> env.id,
                              ).!++("payload" -> message.getData.toStringUtf8),
                            )
                            consumer.nack()
                            ()
                        }

                    case Left(ex) =>
                      logger.logError(
                        s"Error processing envelope message!",
                        ex,
                        Log(
                          "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                          "topic" -> topic,
                          "subscription" -> subscription,
                          "id" -> env.id,
                        ).!++("payload" -> message.getData.toStringUtf8),
                      )
                      consumer.nack()
                      ()
                  }

              case Left(ex) =>
                logger.logError(
                  s"Error processing PubSubMessage!",
                  ex,
                  Log(
                    "topic" -> topic,
                    "subscription" -> subscription,
                  ).!++("payload" -> message.getData.toStringUtf8),
                )
                consumer.nack()
                ()
            }
        }
  }

  private case class TerminalFailureApiServiceListener(subscription: String, restartFn: () => Future[Unit])
    extends ApiService.Listener {

    override def failed(from: ApiService.State, ex: Throwable): Unit = {
      logger.logError(s"Terminal failure in the subscriber! ($subscription)", ex)
      closeSubscriber(subscription)
      restartFn()
      ()
    }
  }
}
