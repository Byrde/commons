package org.byrde.slick.migrations.api
import org.byrde.slick.migrations.engine.Migration

trait NamedMigration {
  def migration: Migration

  def name: String =
    this.getClass.getSimpleName
}
