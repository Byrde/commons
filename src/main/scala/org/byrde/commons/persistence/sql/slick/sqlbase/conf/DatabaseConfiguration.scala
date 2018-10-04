package org.byrde.commons.persistence.sql.slick.sqlbase.conf

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DatabaseConfiguration {
  def jdbc: DatabaseConfig[JdbcProfile]
}
