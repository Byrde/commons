package org.byrde.slick.conf

import scala.concurrent.duration.Duration

case class SlickMigrationEngineConfig(createMigrationTableRetryDelay: Duration, checkClusteredMigrationCompleteDelay: Duration)
