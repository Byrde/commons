package org.byrde.smtp

import org.byrde.smtp.conf.SmtpConfig
import org.byrde.support.Email

import javax.mail.internet._
import javax.mail.{ Message, Transport }

import scala.util.chaining._

import org.jsoup.Jsoup

class SmtpClient(config: SmtpConfig) {
  def send(request: SmtpRequest): Unit = Transport.send(buildEmail(request), config.user, config.password)

  private def buildEmail(request: SmtpRequest): MimeMessage =
    buildEmail(request.recipient, request.subject)(buildBody(request))

  private def buildEmail(recipient: Email, subject: String)(mimeMultipart: MimeMultipart): MimeMessage =
    new MimeMessage(config.session)
      .tap(_.setContent(mimeMultipart))
      .tap(_.setFrom(new InternetAddress(config.from.toString)))
      .tap(_.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.toString)))
      .tap(_.setSubject(subject))

  private def buildBody(request: SmtpRequest): MimeMultipart =
    new MimeMultipart("alternative")
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(Jsoup.parse(request.htmlContent).body.text, "utf-8"))
      })
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(request.htmlContent, "utf-8", "html"))
      })
}
