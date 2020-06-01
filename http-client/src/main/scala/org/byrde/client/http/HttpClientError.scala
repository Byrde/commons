package org.byrde.client.http

import io.circe.{DecodingFailure, ParsingFailure}

class HttpClientError(response: Response)(throwable: Throwable) extends Throwable(throwable)

object HttpClientError {

  case class HttpExecutorError(response: Response)(throwable: Throwable) extends HttpClientError(response)(throwable)

  case class HttpResponseError(response: Response) extends HttpClientError(response)(new Exception("Request Failed"))

  case class HttpParsingError(response: Response)(failure: ParsingFailure) extends HttpClientError(response)(failure)

  case class HttpDecodingError(response: Response)(failure: DecodingFailure) extends HttpClientError(response)(failure)

  case class HttpServiceResponseError(response: Response)(code: Long) extends HttpClientError(response)(new Exception(s"Request Failed. Error Code: $code"))

  case class HttpTimeoutError(response: Response) extends HttpClientError(response)(new Exception("Timed Out"))

}