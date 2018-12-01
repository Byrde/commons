package org.byrde.clients.ahc.impl

import org.byrde.clients.ahc.AhcExecutor
import org.byrde.clients.ahc.impl.JsonAhcExecutor.JsParsingError
import org.byrde.clients.circuitbreaker.CircuitBreakerLike
import org.byrde.clients.circuitbreaker.impl.ClientCircuitBreaker
import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.utils.ServiceResponseUtils._
import org.byrde.service.response.{ServiceResponse, ServiceResponseType}
import org.byrde.uri.Path
import org.byrde.utils.JsonUtils

import akka.actor.ActorSystem

import play.api.libs.json._
import play.api.libs.ws.{BodyWritable, StandaloneWSRequest, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}
import scala.util.control.NoStackTrace

abstract class JsonAhcExecutor extends AhcExecutor {
  self =>
  import JsonAhcExecutor.JsValue2ServiceResponseError

  private case class ModelValidationException[A: ClassTag](errors: Seq[(JsPath, Seq[play.api.libs.json.JsonValidationError])])
    extends Throwable(
      s"""
         |Error parsing: ${classTag[A].runtimeClass},
         |errors: [${formatErrors(errors)}]""".stripMargin) with NoStackTrace

  val circuitBreaker: CircuitBreakerLike =
    new ClientCircuitBreaker(
      name,
      system.scheduler,
      config.circuitBreakerConfig
    )(org.byrde.utils.ThreadPools.Trampoline)

  def ec: ExecutionContext

  def system: ActorSystem

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

  private def formatErrors(errors: Seq[(JsPath, Seq[play.api.libs.json.JsonValidationError])]): String =
    errors.foldLeft("") {
      case (acc, err) =>
        val error =
          s"(path: ${err._1.toString()}, errors: [${err._2.map(_.messages.mkString(" ")).mkString(", ")}])"

        if (acc.isEmpty)
          error
        else
          s"$acc, $error"
    }
}

object JsonAhcExecutor {
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
