package org.byrde.pubsub.google

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

/** Trait providing admin client creation for Google Cloud Pub/Sub.
  *
  * This trait is used internally by Google Pub/Sub implementations to create admin clients for topic and subscription
  * management.
  */
trait GoogleAdminClient {
  private var channel: ManagedChannel = _

  protected def _createTopicAdminClient(
    credentialsProvider: CredentialsProvider,
    hostOpt: Option[String],
  ): TopicAdminClient = {
    val topicSettingsBuilder = TopicAdminSettings.newBuilder()
    transportChannel(hostOpt) match {
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
    hostOpt: Option[String],
  ): SubscriptionAdminClient = {
    val subscriptionSettings = SubscriptionAdminSettings.newBuilder()
    transportChannel(hostOpt) match {
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

  private def transportChannel(hostOpt: Option[String]): Option[TransportChannelProvider] =
    hostOpt.map { pubsubHost =>
      if (channel != null) FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
      else {
        channel = ManagedChannelBuilder.forTarget(pubsubHost).usePlaintext().build()
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
      }
    }
}
