package org.byrde.email.conf

import java.util.Properties

import com.typesafe.config.Config
import javax.mail.{PasswordAuthentication, Session}

import scala.util.{ChainingSyntax, Try}

case class EmailConfig(host: String, email: String, password: String, port: Int, from: String) extends ChainingSyntax {
  private def properties: Properties =
    new Properties()
      .tap(_.put("mail.smtp.host", host))
      .tap(_.put("mail.smtp.port", port.toString))
      .tap(_.put("mail.smtp.auth", "true"))
      .pipe { props =>
        if (port == 587)
          props
            .tap(_.put("mail.smtp.starttls.enable", "true"))
        else if (port == 465)
          props
            .tap(_.put("mail.smtp.ssl.enable", "true"))
            .tap(_.put("mail.smtp.socketFactory.port", port.toString))
            .tap(_.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"))
        else
          props
      }

  def sessionFromConfig: Session =
    Session.getInstance(
      properties,
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(email, password)
      }
    )
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
