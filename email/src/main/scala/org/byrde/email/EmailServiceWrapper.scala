package org.byrde.email

import java.util.Date

import org.byrde.email.conf.EmailConfig

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

import scala.util.{Failure, Success, Try}

class EmailServiceWrapper(emailConfig: EmailConfig) {
  def sendMessage(recipient: String, subject: String, content: String): Unit =
    Try(Transport.send(buildMessage(recipient, subject, content))) match {
      case Success(_) =>
        ()

      case Failure(ex) =>
        ex.printStackTrace()
        ()
    }

  private def buildBody(content: String): MimeMultipart = {
    val messageBodyPart =
      new MimeBodyPart()

    val multipart =
      new MimeMultipart()

    messageBodyPart.setContent(content, "text/html;charset=utf-8")

    multipart.addBodyPart(messageBodyPart)

    multipart
  }

  private def buildMessage(recipient: String, subject: String, content: String): MimeMessage =
    buildMessage(recipient, subject)(buildBody(content))

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
}
