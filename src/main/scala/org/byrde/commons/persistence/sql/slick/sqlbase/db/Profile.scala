package org.byrde.commons.persistence.sql.slick.sqlbase.db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait Profile {
	val dbConfig: DatabaseConfig[JdbcProfile]
	implicit val profile: JdbcProfile = dbConfig.profile
}
