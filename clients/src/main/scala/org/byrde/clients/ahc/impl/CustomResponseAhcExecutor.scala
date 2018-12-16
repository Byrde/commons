package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.impl.JsonAhcExecutor.JsParsingError
import org.byrde.uri.Path
import org.byrde.utils.exceptions.ModelValidationException

import play.api.libs.json._
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class CustomResponseAhcExecutor extends JsonAhcExecutor {
  self =>
  def get[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => T] = None)(implicit reads: Reads[T]): Future[T] =
    super.getJson(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  def post[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => TT] = None)(implicit writes: Writes[T], reads: Reads[TT]): Future[TT] =
    super.postJson(Json.toJson(body))(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def put[T, TT: ClassTag](body: T)(path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => TT] = None)(implicit writes: Writes[T], reads: Reads[TT]): Future[TT] =
    super.putJson(Json.toJson(body))(path, requestHook, curlRequestHook).map(processResponse[TT](_, errorHook))(ec)

  def delete[T: ClassTag](path: Path, requestHook: StandaloneWSRequest => StandaloneWSRequest = identity, curlRequestHook: CurlRequest => Unit = _ => (), errorHook: Option[JsParsingError => T] = None)(implicit reads: Reads[T]): Future[T] =
    super.deleteJson(path, requestHook, curlRequestHook).map(processResponse[T](_, errorHook))(ec)

  private def processResponse[T: ClassTag](json: JsValue, errorHook: Option[JsParsingError => T])(implicit reads: Reads[T]): T =
    json
      .validate[T] match {
        case JsSuccess(validated, _) =>
          validated

        case err: JsError =>
          errorHook
            .fold(throw ModelValidationException[T](err.errors)) { func =>
              func(JsParsingError(json, err))
            }
      }
}