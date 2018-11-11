package org.byrde.slick.conf

import scala.concurrent.duration.Duration

case class MigrationEngineConfig(createMigrationTableRetryDelay: Duration, checkClusteredMigrationCompleteDelay: Duration)
