package org.byrde.commons.models.email

import java.sql.Timestamp

import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0200
import org.byrde.commons.models.services.ServiceResponse

import play.api.libs.json.{JsObject, Json, Writes}
import play.twirl.api.Html

class EmailResponse(
  val subject: String,
  val emailRecipient: String,
  val content: Html,
  val timestamp: Timestamp)

object EmailResponse {
  implicit val writes: Writes[EmailResponse] = new Writes[EmailResponse] {
    override def writes(o: EmailResponse): JsObject = Json.obj(
      "subject" -> o.subject,
      "emailRecipient" -> o.emailRecipient,
      "timestamp" -> o.timestamp.toLocalDateTime.toString
    )
  }

  def apply(
   subject: String,
   emailRecipient: String,
   content: Html,
   timestamp: Timestamp): ServiceResponse[EmailResponse] =
    E0200.withResponse(new EmailResponse(subject, emailRecipient, content, timestamp))
}