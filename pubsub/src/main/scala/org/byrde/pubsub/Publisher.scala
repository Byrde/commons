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

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.Try
import scala.util.chaining._

abstract class Publisher(logger: Logger)(implicit ec: ExecutionContextExecutor)
  extends JavaFutureSupport
    with AdminClient
    with MutexSupport
    with AutoCloseable {

  private type Topic = String

  private val _ackDeadline = 10 // seconds

  private val _publishers: scala.collection.concurrent.Map[Topic, com.google.cloud.pubsub.v1.Publisher] = TrieMap.empty

  def createTopic(
    credentials: Credentials,
    project: String,
    topic: String,
    hostOpt: Option[String],
  ): Unit =
    _createTopicAdminClient(FixedCredentialsProvider.create(credentials), hostOpt).pipe { client =>
      Try {
        client
          .tap(_.createTopic(TopicName.ofProjectTopicName(project, topic).toString))
          .tap(_.shutdown())
          .awaitTermination(_ackDeadline, TimeUnit.SECONDS)
        logger.logInfo(s"Created topic: $topic")
        ()
      }.recover {
        case _: AlreadyExistsException =>
          logger.logDebug(s"Topic already exists: $topic")
          client.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
          client.close()
          ()

        case ex =>
          client.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
          client.close()
          throw ex
      }
    }

  @tailrec
  final def publish[T](
    credentials: Credentials,
    project: String,
    env: Envelope[T],
    hostOpt: Option[String] = None,
  )(implicit encoder: Encoder[T]): Future[Unit] =
    _publishers.get(env.topic) match {
      case Some(publisher) =>
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
            _publishers
              .get(env.topic)
              .foreach {
                case innerPublisher if innerPublisher == publisher =>
                  ()

                case innerPublisher =>
                  logger.logWarning(s"Multiple instances of the same publisher type detected! (${env.topic})")
                  innerPublisher.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
                  ()
              }
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

      case None if !_locked.get() =>
        mutex {
          _publishers.getOrElseUpdate(env.topic, createTopicAndPublisher(credentials, project, env, hostOpt))
        }
        publish(credentials, project, env, hostOpt)

      case _ =>
        publish(credentials, project, env, hostOpt)
    }

  override def close(): Unit = {
    logger.logInfo("Shutting down all publishers...")
    _publishers.foreach {
      case (topic, publisher) =>
        _publishers.remove(topic)
        publisher.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
    }
  }

  private def closePublisher(topic: Topic): Unit =
    _publishers
      .get(topic)
      .foreach { publisher =>
        logger.logInfo(s"Shutting down publisher: $topic")
        _publishers.remove(topic)
        publisher.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS)
      }

  private def createTopicAndPublisher[T](
    credentials: Credentials,
    project: String,
    env: Envelope[T],
    hostOpt: Option[String],
  ): com.google.cloud.pubsub.v1.Publisher = {
    createTopic(credentials, project, env.topic, hostOpt)
    logger.logInfo(s"Creating publisher: ${env.topic}")
    publisher(credentials, project, env.topic, env.orderingKey.isDefined, hostOpt)
  }

  private def publisher(
    credentials: Credentials,
    project: String,
    topic: String,
    enableMessageOrdering: Boolean,
    hostOpt: Option[String],
  ): com.google.cloud.pubsub.v1.Publisher =
    com
      .google
      .cloud
      .pubsub
      .v1
      .Publisher
      .newBuilder(TopicName.ofProjectTopicName(project, topic).toString)
      .setEnableMessageOrdering(enableMessageOrdering)
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

}
