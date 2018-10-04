package org.byrde.commons.persistence.sql.slick.sqlbase.conf

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

case class Profile(jdbc: DatabaseConfig[JdbcProfile]) extends DatabaseConfiguration {
	implicit val profile: JdbcProfile =
		jdbc.profile

	implicit val api: profile.API =
		profile.api
}
