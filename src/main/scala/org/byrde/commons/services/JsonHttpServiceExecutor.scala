package org.byrde.commons.services

import org.byrde.commons.models.services.ServiceResponse.TransientServiceResponse
import org.byrde.commons.models.services.{ServiceResponse, ServiceResponseType}
import org.byrde.commons.models.uri.Path
import org.byrde.commons.services.JsonHttpServiceExecutor._
import org.byrde.commons.services.circuitbreaker.CircuitBreakerLike
import org.byrde.commons.utils.JsonUtils
import org.byrde.commons.utils.ServiceResponseUtils._
import org.byrde.commons.utils.exception.ModelValidationException

import play.api.libs.json._
import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class JsonHttpServiceExecutor extends HttpServiceExecutor {
  self =>

  def ec: ExecutionContext

  def circuitBreaker: CircuitBreakerLike

  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[T]] = None)(implicit format: Format[T]): Future[ServiceResponse[T]] =
    super.underlyingGet(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[TT]] = None)(implicit bodyWritable: BodyWritable[T], format: Format[TT]): Future[ServiceResponse[TT]] =
    super.underlyingPost(body)(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[TT]] = None)(implicit bodyWritable: BodyWritable[T], format: Format[TT]): Future[ServiceResponse[TT]] =
    super.underlyingPut(body)(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[T]] = None)(implicit format: Format[T]): Future[ServiceResponse[T]] =
    super.underlyingDelete(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  override def executeRequest(request: StandaloneWSRequest): Future[StandaloneWSResponse] =
    circuitBreaker.withCircuitBreaker(request.execute().map(identity)(ec))

  private def processResponse[T: ClassTag](response: StandaloneWSResponse, errorHook: Option[JsParsingError => ServiceResponse[T]])(implicit format: Format[T]): ServiceResponse[T] =
    Json
      .parse(response.body)
      .errorHook
      .validate[TransientServiceResponse[T]](ServiceResponse.reads(format)) match {
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
  case class JsParsingError(res: StandaloneWSResponse, err: JsError)

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
