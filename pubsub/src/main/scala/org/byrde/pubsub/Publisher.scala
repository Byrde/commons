package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{AlreadyExistsException, FixedTransportChannelProvider}
import com.google.auth.Credentials
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{PubsubMessage, TopicName}

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

import io.grpc.ManagedChannelBuilder

import org.byrde.logging.{Log, Logger}
import org.byrde.support.JavaFutureSupport

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._

trait Publisher extends JavaFutureSupport with AdminClient with AutoCloseable {
  private val _ackDeadline = 10 //seconds
  
  private val _publishers: mutable.Map[String, com.google.cloud.pubsub.v1.Publisher] =
    mutable.Map()
  
  def createTopic(
    credentials: Credentials,
    project: String,
    topic: String,
    maybeHost: Option[String]
  )(implicit logger: Logger): Future[Unit] =
    _createTopicAdminClient(FixedCredentialsProvider.create(credentials), maybeHost)
      .pipe { client =>
        Future {
          logger.logInfo(s"Creating topic: $topic")
          client
            .tap(_.createTopic(TopicName.ofProjectTopicName(project, topic).toString))
            .tap(_.shutdown())
            .awaitTermination(_ackDeadline, TimeUnit.SECONDS)
        }
        .map(_ => ())
        .recoverWith {
          case _: AlreadyExistsException =>
            Future {
              client
                .tap(_.shutdown())
                .awaitTermination(_ackDeadline, TimeUnit.SECONDS)
            }

          case ex =>
            Future {
              client
                .tap(_.shutdown())
                .awaitTermination(_ackDeadline, TimeUnit.SECONDS)
            }.flatMap(_ => Future.failed(ex))
        }
      }
  
  def publish[T](
    credentials: Credentials,
    project: String,
    env: Envelope[T],
    maybeHost: Option[String] = None,
  )(implicit logger: Logger, encoder: Encoder[T]): Future[Unit] =
    _publishers
      .get(env.topic)
      .map(Future.successful)
      .getOrElse {
        for {
          _ <- createTopic(credentials, project, env.topic, maybeHost)
          publisher <-
            Future {
              logger.logInfo(s"Creating publisher: ${env.topic}")
              val publisherBuilder = com.google.cloud.pubsub.v1.Publisher
                .newBuilder(TopicName.ofProjectTopicName(project, env.topic).toString)
              maybeHost match {
                case Some(host) =>
                  val channel = ManagedChannelBuilder.forTarget(host).usePlaintext.build()
                  publisherBuilder
                    .setChannelProvider(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
                    .setCredentialsProvider(NoCredentialsProvider.create())

                case None =>
                  publisherBuilder
                    .setCredentialsProvider {
                      new CredentialsProvider {
                        override def getCredentials: Credentials = credentials
                      }
                    }
              }
              publisherBuilder.build()
            }
          _ <-
            Future(_publishers.update(env.topic, publisher))
        } yield publisher
      }
      .map { publisher =>
        publisher
          .publish(PubsubMessage.newBuilder.setData(ByteString.copyFromUtf8(env.asJson.toString)).build)
          .asScala
          .map { _ =>
            logger.logDebug(
              s"Message published successfully!",
              Log(
                "correlation-id" -> env.correlationId.getOrElse("No Correlation Id!"),
                "topic" -> env.topic,
                "id" -> env.id,
                "payload" -> env.msg.asJson.noSpaces
              )
            )
            ()
          }
      }
  
  override def close(): Unit =
    _publishers.values.foreach(_.tap(_.shutdown()).awaitTermination(_ackDeadline, TimeUnit.SECONDS))
}

object Publisher extends Publisher