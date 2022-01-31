package org.byrde.smtp

import org.byrde.support.types.Email

case class SmtpRequest(
  recipient: Email,
  subject: String,
  htmlContent: String
)