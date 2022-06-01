package org.byrde.pubsub

import com.google.api.gax.core.{CredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{FixedTransportChannelProvider, TransportChannelProvider}
import com.google.cloud.pubsub.v1.{SubscriptionAdminClient, SubscriptionAdminSettings, TopicAdminClient, TopicAdminSettings}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.byrde.logging.Logger

import java.util.concurrent.TimeUnit
import scala.util.Try

trait AdminClientTrait {
  private def transportChannel(maybeHost: Option[String])(implicit logger: Logger): Option[TransportChannelProvider] =
    maybeHost.flatMap(pubsubHost => {
      val channel: ManagedChannel = ManagedChannelBuilder.forTarget(pubsubHost).usePlaintext().build()
      val tryTransport = Try {
        val isSuccess = channel.awaitTermination(5, TimeUnit.SECONDS)
        logger.logInfo("Creating channel isSuccess: " + isSuccess)
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
      }
      tryTransport.failed.foreach { throwable =>
        logger.logError(throwable.getMessage)
      }
      tryTransport.toOption
    })
  
  protected def _createTopicAdminClient(credentialsProvider: CredentialsProvider, maybeHost: Option[String])(implicit logger: Logger): TopicAdminClient = {
    val topicSettingsBuilder = TopicAdminSettings.newBuilder()
    transportChannel(maybeHost) match {
      case None =>
        TopicAdminClient.create(
          topicSettingsBuilder
            .setCredentialsProvider(credentialsProvider)
            .build())
      case Some(transport) =>
        TopicAdminClient.create(
          topicSettingsBuilder
            .setTransportChannelProvider(transport)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build())
    }
  }
  
  protected def _createSubscriptionAdminClient(credentialsProvider: CredentialsProvider, maybeHost: Option[String])(implicit logger: Logger): SubscriptionAdminClient = {
    val subscriptionSettings = SubscriptionAdminSettings.newBuilder()
    transportChannel(maybeHost) match {
      case None =>
        SubscriptionAdminClient.create(
          subscriptionSettings
            .setCredentialsProvider(credentialsProvider)
            .build())
      case Some(transport) => SubscriptionAdminClient.create(
        subscriptionSettings
          .setTransportChannelProvider(transport)
          .setCredentialsProvider(NoCredentialsProvider.create())
          .build())
    }
  }
}
