package org.byrde.email

import java.sql.Timestamp

import org.byrde.email.conf.EmailConfig
import org.byrde.email.response.EmailResponse
import org.byrde.service.response.ServiceResponse

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

import play.twirl.api.Html

class EmailServiceWrapper(emailConfig: EmailConfig) {
  def sendMessage(recipient: String,
                  subject: String,
                  content: Html): ServiceResponse[EmailResponse] = {
    val message =
      new MimeMessage(emailConfig.sessionFromConfig)

    val multipart =
      new MimeMultipart()

    val messageBodyPart =
      new MimeBodyPart()

    message.setFrom(new InternetAddress(emailConfig.from))
    message.setRecipients(Message.RecipientType.TO, recipient)
    message.setSubject(subject)

    messageBodyPart.setContent(content.body, "text/html;charset=utf-8")

    multipart.addBodyPart(messageBodyPart)

    message.setContent(multipart)

    Transport.send(message)

    EmailResponse(subject,
                  recipient,
                  content,
                  new Timestamp(System.currentTimeMillis))
  }
}
