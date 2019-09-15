package org.byrde.slick.migrations

import org.byrde.slick.Role.Master
import org.byrde.slick.conf.DatabaseConfig

import slick.jdbc.JdbcProfile
import slick.migration.api.Dialect

trait Migrations {
  def apply[P <: JdbcProfile](config: DatabaseConfig[Master])(implicit dialect: Dialect[P]): Seq[NamedMigration]
}
