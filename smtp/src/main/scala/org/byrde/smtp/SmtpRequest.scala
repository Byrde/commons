package org.byrde.smtp

case class SmtpRequest(
  recipient: String,
  subject: String,
  htmlContent: String
)