package org.byrde.email.response

case class EmailResponse(subject: String, emailRecipient: String, content: String)