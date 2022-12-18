package org.byrde.pubsub

import org.byrde.logging.{ Log, Logger }
import org.byrde.support.JavaFutureSupport

import com.google.api.gax.core.{ CredentialsProvider, FixedCredentialsProvider, NoCredentialsProvider }
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{ AlreadyExistsException, FixedTransportChannelProvider }
import com.google.auth.Credentials
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{ PubsubMessage, TopicName }

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import io.grpc.ManagedChannelBuilder

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import scala.collection.mutable
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.chaining._

abstract class Publisher(logger: Logger)(implicit ec: ExecutionContextExecutor)
  extends JavaFutureSupport
    with AdminClient
    with AutoCloseable {

  private type Topic = String

  private val _ackDeadline = 10 // seconds

  private val _publishers: AtomicReference[mutable.Map[Topic, com.google.cloud.pubsub.v1.Publisher]] =
    new AtomicReference(mutable.Map())

  def createTopic(
    credentials: Credentials,
    project: String,
    topic: String,
    hostOpt: Option[String],
  ): Future[Unit] =
    _createTopicAdminClient(FixedCredentialsProvider.create(credentials), hostOpt).pipe { client =>
      Future {
        client
          .tap(_.createTopic(TopicName.ofProjectTopicName(project, topic).toString))
          .tap(_.shutdown())
          .awaitTermination(_ackDeadline, TimeUnit.SECONDS)
        logger.logInfo(s"Created topic: $topic")
        ()
      }.recoverWith {
        case _: AlreadyExistsException =>
          logger.logDebug(s"Topic already exists: $topic")
          Future {
            client.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
          }

        case ex =>
          Future {
            client.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
          }.flatMap(_ => Future.failed(ex))
      }
    }

  def publish[T](
    credentials: Credentials,
    project: String,
    env: Envelope[T],
    hostOpt: Option[String] = None,
  )(implicit encoder: Encoder[T]): Future[Unit] =
    _publishers
      .get()
      .get(env.topic)
      .map { publisher =>
        publisher
          .publish {
            PubsubMessage
              .newBuilder
              .setMessageId(env.id)
              .setData(ByteString.copyFromUtf8(env.asJson.toString))
              .pipe(builder => env.correlationId.fold(builder)(builder.putAttributes("correlationId", _)))
              .pipe(builder => env.orderingKey.fold(builder)(builder.setOrderingKey))
              .build
          }
          .asScala
          .map { _ =>
            logger.logDebug(
              s"Message published successfully!",
              Log(
                "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                "topic" -> env.topic,
                "id" -> env.id,
              ).!++("payload" -> env.msg.asJson.noSpaces),
            )
            ()
          }
          .recoverWith {
            case ex =>
              logger.logError(
                s"Failed to publish message!",
                ex,
                Log(
                  "correlation_id" -> env.correlationId.getOrElse("No Correlation Id!"),
                  "topic" -> env.topic,
                  "id" -> env.id,
                ).!++("payload" -> env.msg.asJson.noSpaces),
              )
              closePublisher(env.topic)
              Future.failed(ex)
          }
      }
      .getOrElse {
        for {
          _ <- createTopic(credentials, project, env.topic, hostOpt)
          _ =
            _publishers.getAndUpdate { _publishers =>
              _publishers
                .get(env.topic)
                .fold {
                  logger.logInfo(s"Creating publisher: ${env.topic}")
                  _publishers.tap(_.update(env.topic, publisher(credentials, project, env.topic, hostOpt)))
                }(_ => _publishers)
            }
        } yield publish(credentials, project, env, hostOpt)
      }

  override def close(): Unit =
    _publishers.getAndUpdate { publishers =>
      logger.logInfo("Shutting down all publishers...")
      publishers.foreach {
        case (_, publisher) =>
          publisher.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
      }
      mutable.Map()
    }

  private def closePublisher(topic: Topic): Unit =
    _publishers.getAndUpdate { publishers =>
      publishers
        .get(topic)
        .fold(publishers) { publisher =>
          logger.logInfo(s"Shutting down publisher: $topic")
          publisher.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
          publishers.tap(_.remove(topic))
        }
    }

  private def publisher(
    credentials: Credentials,
    project: String,
    topic: String,
    hostOpt: Option[String],
  ): com.google.cloud.pubsub.v1.Publisher =
    com
      .google
      .cloud
      .pubsub
      .v1
      .Publisher
      .newBuilder(TopicName.ofProjectTopicName(project, topic).toString)
      .tap { builder =>
        hostOpt match {
          case Some(host) =>
            val channel = ManagedChannelBuilder.forTarget(host).usePlaintext.build()
            builder
              .setChannelProvider(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
              .setCredentialsProvider(NoCredentialsProvider.create())

          case None =>
            builder.setCredentialsProvider {
              new CredentialsProvider {
                override def getCredentials: Credentials = credentials
              }
            }
        }
      }
      .build()
}
