package org.byrde.slick.migrations

import slick.migration.api.Migration

trait NamedMigration {
  def migration: Migration
  def name: String = this.getClass.getSimpleName
}