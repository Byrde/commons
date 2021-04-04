package org.byrde.smtp

import org.byrde.smtp.conf.SmtpConfig

import java.util.Date
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Session, Transport}

import org.jsoup.Jsoup

import scala.util.ChainingSyntax

class Smtp(config: SmtpConfig) extends ChainingSyntax {
  def send(request: SmtpRequest): Unit =
    Transport.send(buildEmail(request), config.user, config.password)

  private def buildEmail(request: SmtpRequest): MimeMessage =
    buildEmail(request.recipient, request.subject)(buildBody(request))

  private def buildEmail(recipient: String, subject: String)(mimeMultipart: MimeMultipart): MimeMessage =
    new MimeMessage(Session.getInstance(config.properties))
      .tap(_.setContent(mimeMultipart))
      .tap(_.setFrom(new InternetAddress(config.from.toString)))
      .tap(_.setReplyTo(Array(new InternetAddress(config.from.toString))))
      .tap(_.setRecipients(Message.RecipientType.TO, recipient))
      .tap(_.setSubject(subject))
      .tap(_.setSentDate(new Date()))

  private def buildBody(request: SmtpRequest): MimeMultipart =
    new MimeMultipart("alternative")
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(Jsoup.parse(request.htmlContent).body.text, "utf-8"))
      })
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(request.htmlContent,"utf-8", "html"))
      })
}
