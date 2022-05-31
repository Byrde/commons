package org.byrde.pubsub

import com.google.api.gax.core.{FixedCredentialsProvider, NoCredentialsProvider}
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.{SubscriptionAdminClient, SubscriptionAdminSettings, TopicAdminClient, TopicAdminSettings}
import org.byrde.logging.Logger

trait AdminClientTrait {
  protected def _createTopicAdminClient(credentials: Credentials)(implicit logger: Logger): TopicAdminClient = {
    val topicSettings = TopicAdminSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
    PubSubChannelProvider() match {
      case None => TopicAdminClient.create(topicSettings.build())
      case Some(transport) => TopicAdminClient.create(
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
    PubSubChannelProvider() match {
      case None => SubscriptionAdminClient.create(subscriptionSettings.build())
      case Some(transport) => SubscriptionAdminClient.create(
        subscriptionSettings
          .setTransportChannelProvider(transport)
          .setCredentialsProvider(NoCredentialsProvider.create())
          .build())
    }
  }
}
