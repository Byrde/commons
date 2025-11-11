package org.byrde.pubsub.google

import org.byrde.logging.{ Log, Logger }
import org.byrde.pubsub._
import org.byrde.pubsub.conf.PubSubConfig

import com.google.api.core.ApiService
import com.google.api.gax.core.{ CredentialsProvider, FixedCredentialsProvider, NoCredentialsProvider }
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{ AlreadyExistsException, FixedTransportChannelProvider }
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.protobuf.Duration
import com.google.pubsub.v1.{ SubscriptionName, TopicName }

import io.circe.Decoder
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.parse
import io.grpc.ManagedChannelBuilder

import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.chaining._
import scala.util.{ Failure, Success, Try }

/** Google Cloud Pub/Sub implementation of MessageSubscriber.
  *
  * @param config
  *   Pub/Sub configuration
  * @param logger
  *   Logger for operation logging
  * @param ec
  *   Execution context
  */
class GooglePubSubSubscriber(
  config: PubSubConfig,
  logger: Logger,
)(implicit ec: ExecutionContextExecutor)
  extends MessageSubscriber
    with GoogleAdminClient
    with AutoCloseable {

  private type Subscription = String

  private val _awaitTermination = 10 // seconds

  private val _subscribers: scala.collection.concurrent.Map[Subscription, com.google.cloud.pubsub.v1.Subscriber] =
    TrieMap.empty

  override def subscribe[T](
    subscription: String,
    topic: String,
  )(
    handler: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Either[PubSubError, Unit]] =
    Try {
      subscribeInternal(
        config.credentials,
        config.project,
        subscription,
        topic,
        config.enableExactlyOnceDelivery,
        config.enableMessageOrdering,
        config.hostOpt,
      )(handler)
    } match {
      case Success(result) => result.map(Right(_))
      case Failure(ex) => Future.successful(Left(PubSubError.SubscriptionError(s"Failed to subscribe to $topic", ex)))
    }

  @tailrec
  private def subscribeInternal[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean,
    enableMessageOrdering: Boolean,
    hostOpt: Option[String],
  )(
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Unit] =
    _subscribers.get(subscription) match {
      case Some(subscriber) =>
        if (!subscriber.isRunning)
          Future(subscriber.startAsync().awaitRunning())
            .map { _ =>
              logger.logDebug(s"Subscriber started successfully!")
              _subscribers
                .get(subscription)
                .foreach {
                  case innerSubscriber if innerSubscriber == subscriber =>
                    ()

                  case innerSubscriber =>
                    logger.logWarning(s"Multiple instances of the same subscriber type detected! ($subscription)")
                    innerSubscriber.stopAsync().awaitTerminated()
                    ()
                }
            }
            .recoverWith {
              case ex =>
                logger.logError("Failed to start the subscriber!", ex)
                closeSubscriber(subscription)
                Future.failed(ex)
            }
        else
          Future.successful(())

      case None =>
        synchronized {
          _subscribers.getOrElseUpdate(
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
        }
        subscribeInternal(
          credentials,
          project,
          subscription,
          topic,
          enableExactlyOnceDelivery,
          enableMessageOrdering,
          hostOpt,
        )(fn)
    }

  override def close(): Unit = {
    logger.logInfo("Shutting down all subscribers...")
    _subscribers.foreach {
      case (subscription, subscriber) =>
        _subscribers.remove(subscription)
        subscriber.stopAsync().awaitTerminated()
    }
  }

  private def closeSubscriber(subscription: String): Unit =
    _subscribers
      .get(subscription)
      .foreach { subscriber =>
        logger.logInfo(s"Shutting down subscriber: $subscription")
        _subscribers.remove(subscription)
        subscriber.stopAsync().awaitTerminated()
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

  private def createSubscription(
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    enableExactlyOnceDelivery: Boolean,
    enableMessageOrdering: Boolean,
    hostOpt: Option[String],
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
              .setAckDeadlineSeconds(10)
              .setMessageRetentionDuration(Duration.newBuilder().setSeconds(604800).build())
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
                  .setMinimumBackoff(Duration.newBuilder().setSeconds(1).build())
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
            builder
              .setChannelProvider {
                FixedTransportChannelProvider
                  .create(GrpcTransportChannel.create(ManagedChannelBuilder.forTarget(host).usePlaintext.build()))
              }
              .setCredentialsProvider(NoCredentialsProvider.create())
        }
      }
      .build()
      .tap { subscriber =>
        subscriber.addListener(
          TerminalFailureApiServiceListener(
            subscription,
            () =>
              subscribeInternal(
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
        .pipe(parse)
        .flatMap(_.as[Envelope[Json]])
        .left
        .map { failure =>
          PubSubError.DecodingError(s"Error decoding envelope: ${message.getData.toStringUtf8}", failure)
        }
        .pipe {
          case Right(env) =>
            env
              .msg
              .as[T]
              .left
              .map { failure =>
                PubSubError.DecodingError(s"Error decoding message: ${message.getData.toStringUtf8}", failure)
              }
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
              Log(
                "topic" -> topic,
                "subscription" -> subscription,
              ).!++("payload" -> message.getData.toStringUtf8),
            )
            consumer.nack()
            ()
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
