package org.byrde.smtp

import org.byrde.commons.types.Email
import org.byrde.smtp.conf.SmtpConfig

import javax.mail.internet._
import javax.mail.{ Message, Transport }

import scala.util.chaining._
import scala.util.{ Failure, Success, Try }

import org.jsoup.Jsoup

/** JavaMail-based implementation of EmailClient.
  *
  * This implementation uses the standard JavaMail API to send emails via SMTP.
  *
  * @param config
  *   SMTP configuration
  */
class JavaMailClient(config: SmtpConfig) extends EmailClient {

  override def send(request: SmtpRequest): Either[SmtpError, Unit] =
    Try {
      val message = buildEmail(request)
      Transport.send(message, config.user, config.password)
    } match {
      case Success(_) => Right(())
      case Failure(ex: javax.mail.AuthenticationFailedException) =>
        Left(SmtpError.AuthenticationError("Authentication failed", ex))
      case Failure(ex: javax.mail.MessagingException) =>
        Left(SmtpError.SendFailure(s"Failed to send email: ${ex.getMessage}", ex))
      case Failure(ex) =>
        Left(SmtpError.SendFailure(s"Unexpected error sending email: ${ex.getMessage}", ex))
    }

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
