package org.byrde.smtp

/** Trait for sending emails.
  *
  * Implementations handle the underlying email infrastructure (SMTP, SendGrid, etc.)
  */
trait EmailClient {

  /** Sends an email.
    *
    * @param request
    *   The email request containing recipient, subject, and content
    * @return
    *   Either containing SmtpError on failure or Unit on success
    */
  def send(request: SmtpRequest): Either[SmtpError, Unit]
}
