package org.byrde.client.http

import io.circe.{DecodingFailure, ParsingFailure}

class HttpClientError(response: ResponseLike)(throwable: Throwable) extends Throwable(throwable)

object HttpClientError {

  case class HttpExecutorError(response: ResponseLike)(throwable: Throwable) extends HttpClientError(response)(throwable)

  case class HttpResponseError(response: ResponseLike) extends HttpClientError(response)(new Exception("Request Failed"))

  case class HttpParsingError(response: ResponseLike)(failure: ParsingFailure) extends HttpClientError(response)(new Exception(failure.message))

  case class HttpDecodingError(response: ResponseLike)(failure: DecodingFailure) extends HttpClientError(response)(new Exception(failure.message))

  case class HttpServiceResponseError(response: ResponseLike)(code: Long) extends HttpClientError(response)(new Exception(s"Request Failed. Error Code: ${code}"))

  case class HttpTimeoutError(response: ResponseLike) extends HttpClientError(response)(new Exception("Timed Out"))

}