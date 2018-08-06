package org.byrde.commons.services

import org.byrde.commons.models.services.{ServiceResponse, ServiceResponseType}
import org.byrde.commons.models.services.ServiceResponse.TransientServiceResponse
import org.byrde.commons.models.uri.Path
import org.byrde.commons.services.circuitbreaker.CircuitBreakerLike
import org.byrde.commons.utils.JsonUtils
import org.byrde.commons.utils.exception.ModelValidationException
import org.byrde.commons.utils.ServiceResponseUtils._
import JsonHttpServiceExecutor._

import play.api.libs.json._
import play.api.libs.ws.{BodyWritable, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

abstract class JsonHttpServiceExecutor(implicit ec: ExecutionContext) extends HttpServiceExecutor {
  self =>

  def returnThreadPool: ExecutionContext

  def circuitBreaker: CircuitBreakerLike

  def get[T: ClassTag](path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => T] = None)(implicit reads: Reads[T]): Future[T] =
    super.underlyingGet(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(returnThreadPool)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => TT] = None)(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPost(body)(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(returnThreadPool)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => TT] = None)(implicit bodyWritable: BodyWritable[T], reads: Reads[TT]): Future[TT] =
    super.underlyingPut(body)(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(returnThreadPool)

  def delete[T: ClassTag](path: Path, requestHook: WSRequest => WSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => T] = None)(implicit reads: Reads[T]): Future[T] =
    super.underlyingDelete(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(returnThreadPool)

  override def executeRequest(request: WSRequest): Future[WSResponse] =
    circuitBreaker.withCircuitBreaker(request.execute().map(identity)(returnThreadPool))

  private def processResponse[T: ClassTag](response: WSResponse, errorHook: Option[JsParsingError => T])(implicit reads: Reads[T]): T =
    Json
      .parse(response.body)
      .errorHook
      .validate[T] match {
        case JsSuccess(validated, _) =>
          validated
        case err: JsError =>
          errorHook
            .fold(throw ModelValidationException[T](err.errors)) { func =>
              func(JsParsingError(response, err))
            }
      }
}

object JsonHttpServiceExecutor {
  case class JsParsingError(res: WSResponse, err: JsError)

  implicit class JsValue2ServiceResponseError(value: JsValue) {
    @inline def errorHook: JsValue =
      value
        .validate[TransientServiceResponse[String]](ServiceResponse.reads(JsonUtils.Format.string(ServiceResponse.message))) match {
          case JsSuccess(validated, _) if validated.`type` == ServiceResponseType.Error =>
            throw validated.toException
          case _ =>
            value
        }
  }
}
