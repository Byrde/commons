package org.byrde.slick.migrations

import slick.migration.api.Migration

trait SlickNamedMigration {
  def migration: Migration
  
  def name: String = this.getClass.getSimpleName
}