package org.byrde.pubsub.conf

import com.google.auth.Credentials

/** Configuration for Google Cloud Pub/Sub client.
  *
  * @param project
  *   Google Cloud project ID
  * @param credentials
  *   Google Cloud credentials
  * @param hostOpt
  *   Optional host for testing (e.g., Pub/Sub emulator)
  * @param enableExactlyOnceDelivery
  *   Enable exactly-once delivery guarantee
  * @param enableMessageOrdering
  *   Enable message ordering by ordering key
  */
case class PubSubConfig(
  project: String,
  credentials: Credentials,
  hostOpt: Option[String] = None,
  enableExactlyOnceDelivery: Boolean = false,
  enableMessageOrdering: Boolean = false,
)
