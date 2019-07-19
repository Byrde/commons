package org.byrde.email.conf

import java.util.Properties

import com.typesafe.config.Config
import javax.mail.{PasswordAuthentication, Session}

import scala.util.Try

case class EmailConfig(host: String, email: String, password: String, port: Int, from: String) {
  def properties: Properties = {
    val props = new Properties()
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", port.toString)
    props.put("mail.smtp.socketFactory.port", port.toString)
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.put("mail.smtp.auth", "true")
    props
  }

  def sessionFromConfig: Session = {
    def authenticator =
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(email, password)
      }

    Session.getInstance(properties, authenticator)
  }
}

object EmailConfig {
  def apply(config: Config): EmailConfig =
    apply("host", "email", "password", "port", "from")(config)

  def apply(_host: String, _email: String, _password: String, _port: String, _from: String)(config: Config): EmailConfig = {
    val host =
      config
        .getString(_host)

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

    EmailConfig(host, email, password, port, from)
  }
}
