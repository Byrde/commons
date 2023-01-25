package org.byrde.smtp.conf

import org.byrde.commons.types.Email
import org.byrde.smtp.SmtpConnectionType

import com.typesafe.config.Config

import java.util.Properties

import javax.mail.{ PasswordAuthentication, Session }

import scala.util.Try
import scala.util.chaining._

case class SmtpConfig(
  host: String,
  user: String,
  password: String,
  from: Email,
  `type`: SmtpConnectionType,
) {
  def port: Int =
    `type` match {
      case SmtpConnectionType.TLS =>
        587

      case SmtpConnectionType.SSL =>
        465

      case SmtpConnectionType.Unsecured =>
        25
    }

  def session: Session =
    new Properties()
      .tap(_.put("mail.smtp.host", host))
      .tap(_.put("mail.smtp.port", port.toString))
      .tap(_.put("mail.smtp.auth", "true"))
      .pipe { props =>
        if (`type` == SmtpConnectionType.TLS)
          props.tap(_.put("mail.smtp.starttls.enable", "true"))
        else if (`type` == SmtpConnectionType.SSL)
          props
            .tap(_.put("mail.smtp.ssl.enable", "true"))
            .tap(_.put("mail.smtp.socketFactory.port", port.toString))
            .tap(_.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"))
        else
          props
      }
      .pipe { props =>
        Session.getDefaultInstance(
          props,
          new javax.mail.Authenticator() {
            override protected def getPasswordAuthentication: PasswordAuthentication =
              new PasswordAuthentication(user, password)
          },
        )
      }
}

object SmtpConfig {

  /** e.g configuration (1): { "host": "smtp.com", "user": "api", "password": "password", "from": "donotreply@smtp.com",
    * "type": "ssl" }
    *
    * e.g configuration (2): { "host": "smtp.com", "user": "api", "password": "password", "from": "donotreply@smtp.com",
    * "port": "465" }
    *
    * *In the case of conflicting type and port values, type will be prioritized.
    *
    * @param config
    *   \- Typesafe config adhering to above examples.
    * @return
    *   \- SmtpConfig
    */
  def apply(config: Config): SmtpConfig = apply("host", "user", "password", "from", "type", "port")(config)

  def apply(
    _host: String,
    _user: String,
    _password: String,
    _from: String,
    _type: String,
    _port: String,
  )(config: Config): SmtpConfig = {
    val host = config.getString(_host)

    val user = config.getString(_user)

    val password = config.getString(_password)

    val from = Email.fromString(config.getString(_from)).fold(_ => throw new Exception("Invalid email!"), identity)

    val typeOpt = Try(config.getString(_type)).toOption

    val portOpt = Try(config.getString(_port)).toOption

    val `type`: SmtpConnectionType =
      (typeOpt, portOpt) match {
        case (Some(innerType), _) if innerType.equalsIgnoreCase("ssl") =>
          SmtpConnectionType.SSL

        case (Some(innerType), _) if innerType.equalsIgnoreCase("tls") =>
          SmtpConnectionType.TLS

        case (Some(innerType), _) if innerType.equalsIgnoreCase("unsecured") =>
          SmtpConnectionType.Unsecured

        case (_, Some("465")) =>
          SmtpConnectionType.SSL

        case (_, Some("587")) =>
          SmtpConnectionType.TLS

        case (_, Some("25")) =>
          SmtpConnectionType.Unsecured

        case _ =>
          throw new Exception("Invalid connection type!")
      }

    SmtpConfig(host, user, password, from, `type`)
  }
}
