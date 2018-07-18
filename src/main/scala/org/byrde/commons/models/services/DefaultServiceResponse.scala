package org.byrde.commons.models.services

import org.byrde.commons.utils.WritesUtils

import play.api.libs.json.Writes

trait DefaultServiceResponse extends ServiceResponse[String] {
  override implicit val writes: Writes[String] =
    WritesUtils.string

  override val response: String =
    msg
}
