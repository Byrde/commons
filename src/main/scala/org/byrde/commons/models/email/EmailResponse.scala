package org.byrde.commons.models.email

import java.sql.Timestamp

import org.byrde.commons.models.services.{ServiceResponse, ServiceResponseType}
import play.api.libs.json.{JsObject, Json, Writes}
import play.twirl.api.Html

class EmailResponse(val subject: String,
                    val emailRecipient: String,
                    val content: Html,
                    val timestamp: Timestamp)

object EmailResponse {
  implicit val writes: Writes[EmailResponse] =
    new Writes[EmailResponse] {
      override def writes(o: EmailResponse): JsObject =
        Json.obj(
          "subject"        -> o.subject,
          "emailRecipient" -> o.emailRecipient,
          "timestamp"      -> o.timestamp.toLocalDateTime.toString
        )
    }

  def apply(subject: String,
            emailRecipient: String,
            content: Html,
            timestamp: Timestamp): ServiceResponse[EmailResponse] =
    new ServiceResponse[EmailResponse] {
      override implicit def writes: Writes[EmailResponse] =
        EmailResponse.writes

      override def `type`: ServiceResponseType =
        ServiceResponseType.Success

      override def msg: String =
        "Email Sent"

      override def status: Int =
        200

      override def code: Int =
        200

      override def response =
        new EmailResponse(subject, emailRecipient, content, timestamp)
    }
}
