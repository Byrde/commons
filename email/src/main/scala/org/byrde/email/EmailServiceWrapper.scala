package org.byrde.email

import java.util.Date

import org.byrde.email.conf.EmailConfig
import org.byrde.email.request.EmailRequest

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

import scala.util.{Failure, Success, Try}

class EmailServiceWrapper(emailConfig: EmailConfig) {
  def sendMessage(request: EmailRequest): Unit =
    Try(Transport.send(buildMessage(request))) match {
      case Success(_) =>
        ()

      case Failure(ex) =>
        ex.printStackTrace()
        ()
    }

  private def buildMessage(request: EmailRequest): MimeMessage =
    buildMessage(request.recipient, request.subject)(buildBody(request))

  private def buildMessage(recipient: String, subject: String)(mimeMultipart: MimeMultipart): MimeMessage = {
    val message =
      new MimeMessage(emailConfig.sessionFromConfig)

    message.setContent(mimeMultipart)
    message.setFrom(new InternetAddress(emailConfig.from))
    message.setRecipients(Message.RecipientType.TO, recipient)
    message.setSentDate(new Date())
    message.setSubject(subject, "utf-8")

    message
  }

  private def buildBody(request: EmailRequest): MimeMultipart = {
    val textBodyPart =
      new MimeBodyPart()

    textBodyPart.setContent(request.textContent,"text/plain")

    val multipart =
      new MimeMultipart("alternative")

    multipart.addBodyPart(textBodyPart)

    request.htmlContent.foreach { htmlContent =>
      val htmlBodyPart =
        new MimeBodyPart()

      textBodyPart.setContent(htmlContent,"text/html")

      multipart.addBodyPart(htmlBodyPart)
    }

    multipart
  }
}
