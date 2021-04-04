package org.byrde.pubsub.conf

import com.typesafe.config.Config

case class PubSubConfig(
  projectId: String,
  clientEmail: String,
  privateKey: String,
  batch: Int = 10,
)

object PubSubConfig {
  /**
   * e.g configuration:
   * {
   *   project-id = "projectId"
   *   client-email = "client@email.com"
   *   private-key = ${privateKey}
   * }
   *
   * @param config - Typesafe config adhering to above example.
   * @return
   */
  def apply(config: Config): PubSubConfig =
    apply("project-id", "client-email", "private-key", config)
  
  def apply(
    _projectId: String,
    _clientEmail: String,
    _privateKey: String,
    config: Config
  ): PubSubConfig =
    new PubSubConfig(
      config.getString(_projectId),
      config.getString(_clientEmail),
      config.getString(_privateKey)
    )
}