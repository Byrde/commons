package org.byrde.commons.utils.email.conf

import java.util.Properties

import javax.mail.{PasswordAuthentication, Session}

import play.api.Configuration

case class EmailConfig(
	email: String,
	password: String,
	port: Int){
	def propertiesFromConfig: Properties = {
		val props = new Properties()
		props.put("mail.smtp.auth", "true")
		props.put("mail.smtp.starttls.enable", "true")
		props.put("mail.smtp.host", "smtp.gmail.com")
		props.put("mail.smtp.port", port.toString)
		props
	}

	def sessionFromConfig: Session = {
		Session.getInstance(propertiesFromConfig,
			new javax.mail.Authenticator() {
				override def getPasswordAuthentication = new PasswordAuthentication(email, password)
			}
		)
	}
}

object EmailConfig{
	def apply(config: Configuration): EmailConfig =
		apply("email", "password", "port")(config)

	def apply(_email: String, _password: String, _port: String)(config: Configuration): EmailConfig = {
		val email =
			config
				.getString(_email)
				.getOrElse (
					throw new Exception(s"Missing configuration value: ${_email}"))
		val password =
			config
				.getString(_password)
				.getOrElse (
					throw new Exception(s"Missing configuration value: ${_password}"))
		val port =
			config.getInt(_port).getOrElse(587)

		EmailConfig(email, password, port)
	}
}



