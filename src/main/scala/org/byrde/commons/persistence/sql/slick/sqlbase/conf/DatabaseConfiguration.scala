package org.byrde.commons.persistence.sql.slick.sqlbase.conf

import org.byrde.commons.persistence.sql.slick.Role

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DatabaseConfiguration[R <: Role] {
  def jdbc: DatabaseConfig[JdbcProfile]
}
