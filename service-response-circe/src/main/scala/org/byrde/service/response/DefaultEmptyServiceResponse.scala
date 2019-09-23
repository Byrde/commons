package org.byrde.service.response

class DefaultEmptyServiceResponse(override val status: Int, override val code: Int) extends EmptyServiceResponse[DefaultEmptyServiceResponse] {
  override def apply(_code: Int): DefaultEmptyServiceResponse =
    new DefaultEmptyServiceResponse(status, _code)
}
