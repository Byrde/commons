package org.byrde.client.http

import io.circe.DecodingFailure


class HttpClientError(response: ResponseLike)(throwable: Throwable) extends Throwable(throwable)

object HttpClientError {

  case class HttpExecutorError(response: ResponseLike)(throwable: Throwable) extends HttpClientError(response)(new Exception("Request Failed"))

  case class HttpResponseError(response: ResponseLike) extends HttpClientError(response)(new Exception("Request Failed"))

  case class HttpParsingError(response: ResponseLike)(failure: DecodingFailure) extends HttpClientError(response)(new Exception(failure.message))

}