package org.byrde.pubsub

import com.google.api.gax.core.{ CredentialsProvider, NoCredentialsProvider }
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{ FixedTransportChannelProvider, TransportChannelProvider }
import com.google.cloud.pubsub.v1.{
  SubscriptionAdminClient,
  SubscriptionAdminSettings,
  TopicAdminClient,
  TopicAdminSettings,
}

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }

trait AdminClient {
  private var channel: ManagedChannel = _

  private def transportChannel(maybeHost: Option[String]): Option[TransportChannelProvider] =
    maybeHost.map { pubsubHost =>
      if (channel != null) FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
      else {
        channel = ManagedChannelBuilder.forTarget(pubsubHost).usePlaintext().build()
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
      }
    }

  protected def _createTopicAdminClient(
    credentialsProvider: CredentialsProvider,
    maybeHost: Option[String],
  ): TopicAdminClient = {
    val topicSettingsBuilder = TopicAdminSettings.newBuilder()
    transportChannel(maybeHost) match {
      case None =>
        TopicAdminClient.create(
          topicSettingsBuilder.setCredentialsProvider(credentialsProvider).build(),
        )

      case Some(transport) =>
        TopicAdminClient.create(
          topicSettingsBuilder
            .setTransportChannelProvider(transport)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build(),
        )
    }
  }

  protected def _createSubscriptionAdminClient(
    credentialsProvider: CredentialsProvider,
    maybeHost: Option[String],
  ): SubscriptionAdminClient = {
    val subscriptionSettings = SubscriptionAdminSettings.newBuilder()
    transportChannel(maybeHost) match {
      case None =>
        SubscriptionAdminClient.create(
          subscriptionSettings.setCredentialsProvider(credentialsProvider).build(),
        )

      case Some(transport) =>
        SubscriptionAdminClient.create(
          subscriptionSettings
            .setTransportChannelProvider(transport)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build(),
        )
    }
  }
}
