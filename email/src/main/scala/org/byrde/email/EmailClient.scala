package org.byrde.email

import java.util.Date
import org.byrde.email.conf.EmailConfig
import org.byrde.email.request.EmailRequest

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

import org.jsoup.Jsoup

import scala.util.ChainingSyntax

class EmailClient(config: EmailConfig) extends ChainingSyntax {
  def sendMessage(request: EmailRequest): Unit =
    Transport.send(buildEmail(request))

  private def buildEmail(request: EmailRequest): MimeMessage =
    buildEmail(request.recipient, request.subject)(buildBody(request))

  private def buildEmail(recipient: String, subject: String)(mimeMultipart: MimeMultipart): MimeMessage =
    new MimeMessage(config.sessionFromConfig)
      .tap(_.setContent(mimeMultipart))
      .tap(_.setFrom(new InternetAddress(config.from)))
      .tap(_.setReplyTo(Array(new InternetAddress(config.from))))
      .tap(_.setRecipients(Message.RecipientType.TO, recipient))
      .tap(_.setSubject(subject))
      .tap(_.setSentDate(new Date()))

  private def buildBody(request: EmailRequest): MimeMultipart =
    new MimeMultipart("alternative")
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(Jsoup.parse(request.htmlContent).body.text, "utf-8"))
      })
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(request.htmlContent,"utf-8", "html"))
      })
}
