package org.byrde.slick.conf

import org.byrde.slick.SlickRole

import slick.jdbc.JdbcProfile

class SlickDatabaseConfig[R <: SlickRole](val jdbc: slick.basic.DatabaseConfig[JdbcProfile])