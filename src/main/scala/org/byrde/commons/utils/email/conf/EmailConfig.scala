package org.byrde.commons.utils.email.conf

import java.util.Properties

import javax.mail.{PasswordAuthentication, Session}

import play.api.Configuration

case class EmailConfig(email: String, password: String, port: Int, from: String) {
  def propertiesFromConfig: Properties = {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", port.toString)
    props
  }

  def sessionFromConfig: Session = {
    Session.getInstance(propertiesFromConfig, new javax.mail.Authenticator() {
      override def getPasswordAuthentication =
        new PasswordAuthentication(email, password)
    })
  }
}

object EmailConfig {
  def apply(config: Configuration): EmailConfig =
    apply("email", "password", "port", "from")(config)

  def apply(_email: String, _password: String, _port: String, _from: String)(config: Configuration): EmailConfig = {
    val email =
      config
        .get[String](_email)

    val password =
      config
        .get[String](_password)

    val port =
      config
        .get[Int](_port)

    val from =
      config
        .getOptional[String](_from)
        .getOrElse(email)

    EmailConfig(email, password, port, from)
  }
}
