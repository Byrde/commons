package org.byrde.commons.services.rest

import org.byrde.commons.utils.exception.ModelValidationException
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class RestServiceExecutor(implicit val ec: ExecutionContext) {
  val serviceName: String

  def prepareExecutor(
      request: WSRequest,
      _timeout: Option[FiniteDuration] = None,
      responseHook: WSResponse => WSResponse = identity): RestService =
    new RestService {
      override protected val requestHolder: WSRequest =
        request

      override val timeout: FiniteDuration =
        _timeout.getOrElse(10 seconds)

      override protected def wrapRequest[A: ClassTag](body: Option[String])(
          req: => Future[WSResponse])(implicit ec: ExecutionContext,
                                      reads: Reads[A]): Future[A] = {
        req.map(responseHook).map { response =>
          Json.parse(response.body).validate[A] match {
            case JsSuccess(validated, _) =>
              validated
            case JsError(errors) =>
              throw ModelValidationException[A](errors)
          }
        }
      }
    }
}
