package org.byrde.smtp

import org.byrde.commons.types.Email

case class SmtpRequest(
  recipient: Email,
  subject: String,
  htmlContent: String,
)
