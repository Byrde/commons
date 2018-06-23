package org.byrde.commons.services

import org.byrde.commons.services.circuitbreaker.HttpServiceCircuitBreaker
import org.byrde.commons.utils.circuitbreaker.conf.CircuitBreakerConfig
import org.byrde.commons.utils.exception.ModelValidationException

import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.libs.ws.{BodyWritable, WSRequest, WSResponse}

import akka.actor.Scheduler

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

abstract class JsonHttpServiceExecutor(implicit ec: ExecutionContext) extends HttpServiceExecutor {
  self =>

  def scheduler: Scheduler

  def circuitBreakerConfig: CircuitBreakerConfig

  def returnThreadPool: ExecutionContext

  val circuitBreaker: HttpServiceCircuitBreaker =
    new HttpServiceCircuitBreaker(name, scheduler, circuitBreakerConfig)(returnThreadPool)

  def get[T: ClassTag](requestBuilder: WSRequest => WSRequest = identity)(implicit reads: Reads[T]): Future[T] =
    super.underlyingGet(requestBuilder).map(processResponse[T])(returnThreadPool)

  def post[T, TT: ClassTag](body: T)(requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPost(body)(requestBuilder).map(processResponse[TT])(returnThreadPool)

  def put[T, TT: ClassTag](body: T)(requestBuilder: WSRequest => WSRequest = identity)(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPut(body)(requestBuilder).map(processResponse[TT])(returnThreadPool)

  def delete[T: ClassTag](requestBuilder: WSRequest => WSRequest = identity)(implicit reads: Reads[T]): Future[T] =
    super.underlyingDelete(requestBuilder).map(processResponse[T])(returnThreadPool)

  override def executeRequest(request: WSRequest): Future[WSResponse] =
    circuitBreaker.withCircuitBreaker(request.execute().map(identity)(returnThreadPool))

  private def processResponse[T: ClassTag](response: WSResponse)(implicit reads: Reads[T]): T =
    Json
      .parse(response.body)
      .validate[T] match {
        case JsSuccess(validated, _) =>
          validated
        case JsError(errors) =>
          throw ModelValidationException[T](errors)
      }
}
