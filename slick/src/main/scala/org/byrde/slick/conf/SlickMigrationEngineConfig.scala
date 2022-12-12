package org.byrde.slick.conf

import scala.concurrent.duration.Duration

class SlickMigrationEngineConfig(
  val createMigrationTableRetryDelay: Duration,
  val checkClusteredMigrationCompleteDelay: Duration,
)
