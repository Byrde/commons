package org.byrde.slick.conf

import org.byrde.slick.Role

import slick.jdbc.JdbcProfile

trait DatabaseConfig[R <: Role] {
  def jdbc: slick.basic.DatabaseConfig[JdbcProfile]
}
