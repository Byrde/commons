package org.byrde.commons.persistence.sql.slick.sqlbase.conf

import org.byrde.commons.persistence.sql.slick.Role

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

case class Profile[R <: Role](jdbc: DatabaseConfig[JdbcProfile]) extends DatabaseConfiguration[R] {
	implicit val profile: JdbcProfile =
		jdbc.profile

	implicit val api: profile.API =
		profile.api
}
