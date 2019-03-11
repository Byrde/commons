package org.byrde.email

import java.sql.Timestamp

import org.byrde.email.conf.EmailConfig
import org.byrde.email.response.EmailResponse

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Transport}

class EmailServiceWrapper(emailConfig: EmailConfig) {
  def sendMessage(recipient: String,
                  subject: String,
                  content: String): EmailResponse = {
    val message =
      new MimeMessage(emailConfig.sessionFromConfig)

    val multipart =
      new MimeMultipart()

    val messageBodyPart =
      new MimeBodyPart()

    message.setFrom(new InternetAddress(emailConfig.from))
    message.setRecipients(Message.RecipientType.TO, recipient)
    message.setSubject(subject)

    messageBodyPart.setContent(content, "text/html;charset=utf-8")

    multipart.addBodyPart(messageBodyPart)

    message.setContent(multipart)

    Transport.send(message)

    EmailResponse(subject,
                  recipient,
                  content,
                  new Timestamp(System.currentTimeMillis))
  }
}
