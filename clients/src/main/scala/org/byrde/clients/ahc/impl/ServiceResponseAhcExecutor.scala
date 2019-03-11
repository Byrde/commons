package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.impl.JsonAhcExecutor.JsParsingError
import org.byrde.clients.utils.JsonUtils
import org.byrde.clients.utils.exceptions.ModelValidationException
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.utils.ServiceResponseUtils._
import org.byrde.service.response.{ServiceResponse, ServiceResponseType}
import org.byrde.uri.Path

import play.api.libs.json._
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class ServiceResponseAhcExecutor extends JsonAhcExecutor {
  self =>
  import ServiceResponseAhcExecutor._

  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[T]] = None)(implicit format: Format[T]): Future[ServiceResponse[T]] =
    super.getJson(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[TT]] = None)(implicit writes: Writes[T], format: Format[TT]): Future[ServiceResponse[TT]] =
    super.postJson(Json.toJson(body))(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[TT]] = None)(implicit writes: Writes[T], format: Format[TT]): Future[ServiceResponse[TT]] =
    super.putJson(Json.toJson(body))(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[T]] = None)(implicit format: Format[T]): Future[ServiceResponse[T]] =
    super.deleteJson(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  def patch[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => ServiceResponse[TT]] = None)(implicit writes: Writes[T], format: Format[TT]): Future[ServiceResponse[TT]] =
    super.patchJson(Json.toJson(body))(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  private def processResponse[T: ClassTag](json: JsValue, errorHook: Option[JsParsingError => ServiceResponse[T]])(implicit format: Format[T]): ServiceResponse[T] =
    json
      .errorHook
      .validate[TransientServiceResponse[T]](ServiceResponse.reads(format)) match {
        case JsSuccess(validated, _) =>
          validated

        case err: JsError =>
          errorHook
            .fold(throw ModelValidationException[T](err.errors)) { func =>
              func(JsParsingError(json, err))
            }
      }
}

object ServiceResponseAhcExecutor {
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
