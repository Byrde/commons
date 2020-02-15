package org.byrde.slick.conf

import org.byrde.slick.SlickRole

import slick.jdbc.JdbcProfile

trait SlickDatabaseConfig[R <: SlickRole] {
  def jdbc: slick.basic.DatabaseConfig[JdbcProfile]
}
