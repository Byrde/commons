package org.byrde.slick.conf

import org.byrde.slick.Role

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DatabaseConfiguration[R <: Role] {
  def jdbc: DatabaseConfig[JdbcProfile]
}
