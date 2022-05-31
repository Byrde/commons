package org.byrde.pubsub

import com.google.api.gax.core.{FixedCredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{FixedTransportChannelProvider, TransportChannelProvider}
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.{SubscriptionAdminClient, SubscriptionAdminSettings, TopicAdminClient, TopicAdminSettings}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.byrde.logging.Logger

trait AdminClientTrait {
  private type HostPort = String
  private val pubsubEmulatorHost = "PUBSUB_EMULATOR_HOST"
  private val emulatorHostPort: HostPort = System.getenv().getOrDefault(pubsubEmulatorHost, "")
  private val transportChannel: Option[(TransportChannelProvider, HostPort)] =
    if (emulatorHostPort.isEmpty) {
      None
    } else {
      val channel: ManagedChannel = ManagedChannelBuilder.forTarget(emulatorHostPort).usePlaintext().build()
      Some(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)), emulatorHostPort)
    }
  
  protected def _createTopicAdminClient(credentials: Credentials)(implicit logger: Logger): TopicAdminClient = {
    val topicSettings = TopicAdminSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
    transportChannel match {
      case None => TopicAdminClient.create(topicSettings.build())
      case Some((transport, hostPort)) =>
        logger.logInfo("Found emulator channel provider with hostPort: " + hostPort)
        TopicAdminClient.create(
        topicSettings
          .setTransportChannelProvider(transport)
          .setCredentialsProvider(NoCredentialsProvider.create())
          .build())
    }
  }
  
  protected def _createSubscriptionAdminClient(credentials: Credentials): SubscriptionAdminClient = {
    val subscriptionSettings = SubscriptionAdminSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
    transportChannel match {
      case None => SubscriptionAdminClient.create(subscriptionSettings.build())
      case Some((transport, _)) => SubscriptionAdminClient.create(
        subscriptionSettings
          .setTransportChannelProvider(transport)
          .setCredentialsProvider(NoCredentialsProvider.create())
          .build())
    }
  }
}
