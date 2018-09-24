package org.byrde.commons.persistence.sql.slick.sqlbase.db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait Profile {
	def jdbcConfiguration: DatabaseConfig[JdbcProfile]

	implicit val profile: JdbcProfile =
		jdbcConfiguration.profile
}
