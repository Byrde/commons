package org.byrde.smtp

/** Base trait for SMTP/Email errors.
  */
sealed trait SmtpError

object SmtpError {

  /** Error during email sending.
    */
  case class SendFailure(message: String, cause: Throwable) extends SmtpError

  /** Invalid configuration.
    */
  case class InvalidConfiguration(message: String) extends SmtpError

  /** Invalid recipient email address.
    */
  case class InvalidRecipient(email: String) extends SmtpError

  /** Invalid sender email address.
    */
  case class InvalidSender(email: String) extends SmtpError

  /** Connection error.
    */
  case class ConnectionError(message: String, cause: Throwable) extends SmtpError

  /** Authentication error.
    */
  case class AuthenticationError(message: String, cause: Throwable) extends SmtpError
}
