package org.byrde.email.conf

import java.util.Properties

import com.typesafe.config.Config
import javax.mail.{PasswordAuthentication, Session}

import scala.util.Try

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
    def authenticator =
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(email, password)
      }

    Session.getInstance(propertiesFromConfig, authenticator)
  }
}

object EmailConfig {
  def apply(config: Config): EmailConfig =
    apply("email", "password", "port", "from")(config)

  def apply(_email: String, _password: String, _port: String, _from: String)(config: Config): EmailConfig = {
    val email =
      config
        .getString(_email)

    val password =
      config
        .getString(_password)

    val port =
      config
        .getInt(_port)

    val from =
      Try(config.getString(_from)).getOrElse(email)

    EmailConfig(email, password, port, from)
  }
}
