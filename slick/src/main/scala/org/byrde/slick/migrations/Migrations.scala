package org.byrde.slick.migrations

import org.byrde.slick.Role.Master
import org.byrde.slick.conf.DatabaseConfig

trait Migrations {
  def apply(config: DatabaseConfig[Master]): Seq[NamedMigration]
}
