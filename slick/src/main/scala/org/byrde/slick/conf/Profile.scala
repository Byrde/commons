package org.byrde.slick.conf

import org.byrde.slick.Role

import slick.jdbc.JdbcProfile

trait Profile[R <: Role] {
	def configuration: DatabaseConfiguration[R]

	implicit val profile: JdbcProfile =
		configuration.jdbc.profile

	implicit val api: profile.API =
		profile.api
}
