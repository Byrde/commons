package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{AlreadyExistsException, FixedTransportChannelProvider}
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.protobuf.Duration
import com.google.pubsub.v1.{SubscriptionName, TopicName}

import io.circe.{Decoder, Json}
import io.circe.generic.auto._
import io.circe.parser.parse

import io.grpc.ManagedChannelBuilder

import org.byrde.logging.{Log, Logger}

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._
import scala.util.chaining._

trait Subscriber extends AdminClient with AutoCloseable {
  private val _ackDeadline = 10 //seconds
  
  private val _oneWeek = 604800 //seconds
  
  private val _awaitTermination = 10 //seconds
  
  private val _subscribers: mutable.Map[String, com.google.cloud.pubsub.v1.Subscriber] =
    mutable.Map()
  
  def createSubscription(
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    maybeHost: Option[String]
  ): Future[Unit] =
    _createSubscriptionAdminClient(FixedCredentialsProvider.create(credentials), maybeHost)
      .pipe { client =>
        Future {
          client
            .tap(_.createSubscription {
              com.google.pubsub.v1.Subscription
                .newBuilder()
                .setName(SubscriptionName.of(project, subscription).toString)
                .setTopic(TopicName.ofProjectTopicName(project, topic).toString)
                .setAckDeadlineSeconds(_ackDeadline)
                .setMessageRetentionDuration(Duration.newBuilder().setSeconds(_oneWeek).build())
                .setExpirationPolicy(com.google.pubsub.v1.ExpirationPolicy.newBuilder().build())
                .setRetryPolicy {
                  com.google.pubsub.v1.RetryPolicy
                    .newBuilder()
                    .setMinimumBackoff(Duration.newBuilder().setSeconds(1).build())
                    .setMaximumBackoff(Duration.newBuilder().setSeconds(180).build())
                }
                .build()
            })
            .tap(_.shutdown())
            .awaitTermination(_awaitTermination, TimeUnit.SECONDS)
        }
        .map(_ => ())
        .recoverWith {
          case _: AlreadyExistsException =>
            Future {
              client
                .tap(_.shutdown())
                .awaitTermination(_awaitTermination, TimeUnit.SECONDS)
            }
  
          case ex =>
            Future {
              client
                .tap(_.shutdown())
                .awaitTermination(_awaitTermination, TimeUnit.SECONDS)
            }.flatMap(_ => Future.failed(ex))
        }
      }
  
  def subscribe[T](
    credentials: Credentials,
    project: String,
    subscription: String,
    topic: String,
    maybeHost: Option[String] = None
  )(fn: Envelope[T] => Future[_])(implicit logger: Logger, decoder: Decoder[T]): Future[Unit] =
    for {
      _ <- createSubscription(credentials, project, subscription, topic, maybeHost)
      subscriber <-
        Future {
          logger.logInfo(s"Creating subscriber: $subscription")
          val subscriberBuilder = com.google.cloud.pubsub.v1.Subscriber
            .newBuilder(SubscriptionName.of(project, subscription).toString, receiver(subscription, topic, fn))
          maybeHost match {
            case None =>
              subscriberBuilder.setCredentialsProvider {
                new CredentialsProvider {
                  override def getCredentials: Credentials = credentials
                }
              }

            case Some(host) =>
              val channel = ManagedChannelBuilder.forTarget(host).usePlaintext.build()
              subscriberBuilder
                .setChannelProvider(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
                .setCredentialsProvider(NoCredentialsProvider.create())
          }
          subscriberBuilder.build()
        }
      _ <-
        Future(subscriber.startAsync().awaitRunning())
          .recoverWith {
            case ex =>
              logger.logError("Error starting subscriber!", ex)
              Future(subscriber.stopAsync().awaitTerminated()).flatMap(_ => Future.failed(ex))
          }
      _ <- Future(_subscribers.update(subscription, subscriber))
    } yield ()
  
  private def receiver[T](
    subscription: String,
    topic: String,
    fn: Envelope[T] => Future[_],
  )(implicit logger: Logger, decoder: Decoder[T]): MessageReceiver = {
    case (message, consumer) =>
      message.getData.toStringUtf8.pipe { data =>
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
                    val rebuiltEnvelope = Envelope(env.topic, value, env.id)
                    fn(rebuiltEnvelope)
                      .map { _ =>
                        logger.logDebug(
                          "Message processed successfully!",
                          Log(
                            "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                            "topic" -> topic,
                            "subscription" -> subscription,
                            "id" -> env.id,
                            "payload" -> message.getData.toStringUtf8
                          )
                        )
                        consumer.ack()
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
                            )
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
                  "payload" -> message.getData.toStringUtf8
                ),
              )
              consumer.nack()
          }
      }
  }
  
  override def close(): Unit =
    _subscribers.values.foreach(_.stopAsync().awaitTerminated())
}

object Subscriber extends Subscriber