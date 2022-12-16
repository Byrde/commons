package org.byrde.pubsub

import org.byrde.logging.{ Log, Logger }

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
import java.util.concurrent.atomic.AtomicReference

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.util._
import scala.util.chaining._

abstract class Subscriber(logger: Logger)(implicit ec: ExecutionContext) extends AdminClient with AutoCloseable {
  private type Subscription = String

  private val _awaitTermination = 10 // seconds

  private val _subscribers: AtomicReference[mutable.Map[Subscription, com.google.cloud.pubsub.v1.Subscriber]] =
    new AtomicReference(mutable.Map())

  def createSubscription(
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    exactlyOnceDelivery: Boolean = false,
    hostOpt: Option[String] = Option.empty,
  ): Future[Unit] =
    _createSubscriptionAdminClient(FixedCredentialsProvider.create(credentials), hostOpt).pipe { client =>
      Future {
        logger.logInfo(s"Creating subscription: $subscription")
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
              .setEnableExactlyOnceDelivery(exactlyOnceDelivery)
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
      }.map(_ => ())
        .recoverWith {
          case _: AlreadyExistsException =>
            Future {
              client.tap(_.shutdown()).awaitTermination(_awaitTermination, TimeUnit.SECONDS)
            }

          case ex =>
            Future {
              client.tap(_.shutdown()).awaitTermination(_awaitTermination, TimeUnit.SECONDS)
            }.flatMap(_ => Future.failed(ex))
        }
    }

  def subscribe[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    hostOpt: Option[String] = None,
  )(
    fn: Envelope[T] => Future[Either[Nack.type, Ack.type]],
  )(implicit decoder: Decoder[T]): Future[Unit] =
    _subscribers
      .get()
      .get(subscription)
      .map { subscriber =>
        if (!subscriber.isRunning) {
          for {
            _ <- createSubscription(credentials, project, subscription, topic, hostOpt = hostOpt)
            _ <-
              Future(subscriber.startAsync().awaitRunning()).recoverWith {
                case ex =>
                  logger.logError("Error starting the subscriber!", ex)
                  logger.logInfo(s"Shutting down subscriber: $subscription")
                  Future(subscriber.stopAsync().awaitTerminated()).flatMap { _ =>
                    _subscribers.getAndUpdate { _subscribers =>
                      _subscribers.remove(subscription)
                      _subscribers
                    }
                    Future.failed(ex)
                  }
              }
          } yield ()

        } else
          Future.successful(())
      }
      .getOrElse {
        _subscribers.getAndUpdate { _subscribers =>
          logger.logInfo(s"Creating subscriber: $subscription")
          val builder =
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

          val subscriber = builder.build()
          _subscribers.update(subscription, subscriber)
          _subscribers
        }
        subscribe(credentials, project, subscription, topic, hostOpt)(fn)
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
                                "payload" -> message.getData.toStringUtf8,
                              ),
                            )
                            consumer.ack()

                          case Left(_) =>
                            logger.logDebug(
                              "Message not acknowledged!",
                              Log(
                                "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                                "topic" -> topic,
                                "subscription" -> subscription,
                                "id" -> env.id,
                                "payload" -> message.getData.toStringUtf8,
                              ),
                            )
                            consumer.nack()
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
                                "payload" -> message.getData.toStringUtf8,
                              ),
                            )
                            consumer.nack()
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
                          "payload" -> message.getData.toStringUtf8,
                        ),
                      )
                      consumer.nack()
                  }

              case Left(ex) =>
                logger.logError(
                  s"Error processing PubSubMessage!",
                  ex,
                  Log(
                    "topic" -> topic,
                    "subscription" -> subscription,
                    "payload" -> message.getData.toStringUtf8,
                  ),
                )
                consumer.nack()
            }
        }
  }

  override def close(): Unit = {
    logger.logInfo("Shutting down all subscribers...")
    _subscribers.get().values.foreach(_.stopAsync().awaitTerminated())
  }
}
