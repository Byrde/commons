package org.byrde.slick

import org.byrde.slick.conf.SlickDatabaseConfig

import slick.jdbc.JdbcProfile

trait SlickProfile[R <: SlickRole] {
	def config: SlickDatabaseConfig[R]

	implicit val profile: JdbcProfile =
		config.jdbc.profile

	implicit val api: profile.API =
		profile.api
}
