package org.byrde.commons.services

import org.byrde.commons.models.uri.Path
import org.byrde.commons.services.circuitbreaker.CircuitBreakerLike
import org.byrde.commons.utils.exception.ModelValidationException

import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.libs.ws.{BodyWritable, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

abstract class JsonHttpServiceExecutor(implicit ec: ExecutionContext) extends HttpServiceExecutor {
  self =>

  def returnThreadPool: ExecutionContext

  def circuitBreaker: CircuitBreakerLike

  def get[T: ClassTag](path: Path, requestBuilder: WSRequest => WSRequest = identity, handleError: (WSResponse, JsError) => T = defaultErrorHandler[T])(implicit reads: Reads[T]): Future[T] =
    super.underlyingGet(path, requestBuilder).map(processResponse[T](_, handleError))(returnThreadPool)

  def post[T, TT: ClassTag](body: T)(path: Path, requestBuilder: WSRequest => WSRequest = identity, handleError: (WSResponse, JsError) => TT = defaultErrorHandler[TT])(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPost(body)(path, requestBuilder).map(processResponse[TT](_, handleError))(returnThreadPool)

  def put[T, TT: ClassTag](body: T)(path: Path, requestBuilder: WSRequest => WSRequest = identity, handleError: (WSResponse, JsError) => TT = defaultErrorHandler[TT])(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPut(body)(path, requestBuilder).map(processResponse[TT](_, handleError))(returnThreadPool)

  def delete[T: ClassTag](path: Path, requestBuilder: WSRequest => WSRequest = identity, handleError: (WSResponse, JsError) => T = defaultErrorHandler[T])(implicit reads: Reads[T]): Future[T] =
    super.underlyingDelete(path, requestBuilder).map(processResponse[T](_, handleError))(returnThreadPool)

  override def executeRequest(request: WSRequest): Future[WSResponse] =
    circuitBreaker.withCircuitBreaker(request.execute().map(identity)(returnThreadPool))

  private def processResponse[T: ClassTag](response: WSResponse, handleError: (WSResponse, JsError) => T)(implicit reads: Reads[T]): T =
    Json
      .parse(response.body)
      .validate[T] match {
        case JsSuccess(validated, _) =>
          validated
        case JsError(errors) =>
          throw ModelValidationException[T](errors)
      }

  private def defaultErrorHandler[T]: (WSResponse, JsError) => T =
    (_, err) =>
      throw ModelValidationException[T](err.errors)
}
