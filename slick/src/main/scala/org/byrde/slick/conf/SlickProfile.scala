package org.byrde.slick.conf

import org.byrde.slick.SlickRole

import slick.jdbc.JdbcProfile

trait SlickProfile[R <: SlickRole] {
	def config: SlickDatabaseConfig[R]

	implicit val profile: JdbcProfile =
		config.jdbc.profile

	implicit val api: profile.API =
		profile.api
}
