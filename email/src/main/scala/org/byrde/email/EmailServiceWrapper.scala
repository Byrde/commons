package org.byrde.email

import java.util.Date

import org.byrde.email.conf.EmailConfig
import org.byrde.email.request.EmailRequest

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

class EmailServiceWrapper(emailConfig: EmailConfig) {
  def sendMessage(request: EmailRequest): Unit =
    Transport.send(buildEmail(request))

  private def buildEmail(request: EmailRequest): MimeMessage =
    buildEmail(request.recipient, request.subject)(buildBody(request))

  private def buildEmail(recipient: String, subject: String)(mimeMultipart: MimeMultipart): MimeMessage = {
    val message =
      new MimeMessage(emailConfig.sessionFromConfig)

    message.setContent(mimeMultipart)
    message.setFrom(new InternetAddress(emailConfig.from))
    message.setReplyTo(Array(new InternetAddress(emailConfig.from)))
    message.setRecipients(Message.RecipientType.TO, recipient)
    message.setSubject(subject)
    message.setSentDate(new Date())

    message
  }

  private def buildBody(request: EmailRequest): MimeMultipart = {
    val textBodyPart = new MimeBodyPart()
    textBodyPart.setText(request.textContent, "utf-8")

    val htmlBodyPart = new MimeBodyPart()
    htmlBodyPart.setContent(request.htmlContent,"text/html; charset=utf-8")

    val multipart = new MimeMultipart("alternative")

    multipart.addBodyPart(textBodyPart)
    multipart.addBodyPart(htmlBodyPart)

    multipart
  }
}
