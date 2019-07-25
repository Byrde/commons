package org.byrde.email.request

case class EmailRequest(recipient: String, subject: String, textContent: String, htmlContent: String)