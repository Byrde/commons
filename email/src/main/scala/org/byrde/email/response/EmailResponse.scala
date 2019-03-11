package org.byrde.email.response

import java.sql.Timestamp

case class EmailResponse(subject: String,
                          emailRecipient: String,
                          content: String,
                          timestamp: Timestamp)