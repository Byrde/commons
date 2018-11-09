package org.byrde.service.response

sealed trait ServiceResponseType {
  def value: String
}

object ServiceResponseType {
  private val success: String =
    "Success"

  private val error: String =
    "Error"

  object Success extends ServiceResponseType {
    override val value: String =
      success
  }

  object Error extends ServiceResponseType {
    override val value: String =
      error
  }

  def apply(value: String): ServiceResponseType =
    value match {
      case x if x.equalsIgnoreCase(success) =>
        Success
      case x if x.equalsIgnoreCase(error) =>
        Error
    }
}
