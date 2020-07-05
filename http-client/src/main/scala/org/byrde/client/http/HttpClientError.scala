package org.byrde.client.http

import io.circe.{DecodingFailure, ParsingFailure}

class HttpClientError(val response: Response)(val throwable: Throwable) extends Throwable(throwable)

object HttpClientError {

  case class HttpExecutorError(override val response: Response)(throwable: Throwable) extends HttpClientError(response)(throwable)

  case class HttpResponseError(override val response: Response) extends HttpClientError(response)(new Exception("Request Failed"))

  case class HttpParsingError(override val response: Response)(failure: ParsingFailure) extends HttpClientError(response)(failure)

  case class HttpDecodingError(override val response: Response)(failure: DecodingFailure) extends HttpClientError(response)(failure)

  case class HttpServiceResponseError(override val response: Response)(code: Long) extends HttpClientError(response)(new Exception(s"Request Failed. Error Code: $code"))

  case class HttpTimeoutError(override val response: Response) extends HttpClientError(response)(new Exception("Timed Out"))

}