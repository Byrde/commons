package org.byrde.slick.conf

import org.byrde.slick.Role

import slick.jdbc.JdbcProfile

trait Profile[R <: Role] {
	def config: DatabaseConfig[R]

	implicit val profile: JdbcProfile =
		config.jdbc.profile

	implicit val api: profile.API =
		profile.api
}
