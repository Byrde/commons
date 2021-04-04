package org.byrde.email.request

case class EmailRequest(recipient: String, subject: String, htmlContent: String)