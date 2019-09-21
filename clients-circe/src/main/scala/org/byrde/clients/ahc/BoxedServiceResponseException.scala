package org.byrde.clients.ahc

import org.byrde.service.response.ServiceResponseType
import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

class BoxedServiceResponseException(
  override val protocol: String,
  override val host: String,
  override val port: Option[String],
  override val method: String,
  override val path: String
)(override val exception: TransientServiceResponseException)
  extends BoxedResponseException(protocol, host, port, method, path)(exception) {
  def message: String =
    exception.toString

  def status: Int =
    exception.status

  def code: Int =
    exception.code

  def `type`: ServiceResponseType =
    exception.`type`

  def isClientException: Boolean =
    status >= 400 && status < 500

  def isServerException: Boolean =
    status >= 500
}
