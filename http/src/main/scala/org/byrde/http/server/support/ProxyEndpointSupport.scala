package org.byrde.http.server.support

import akka.http.scaladsl.server.Directives.{complete, extractRequest, failWith, onComplete}
import akka.http.scaladsl.server.{Directive1, Route}

import org.byrde.http.SttpClient
import org.byrde.http.server._
import org.byrde.logging.Logger
import org.byrde.uri.Url

import io.circe.{Decoder, Encoder}
import io.circe.parser._

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.client3
import sttp.client3.BodySerializer
import sttp.tapir.Endpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.util.{Failure, Success}

trait ProxyEndpointSupport extends RequestSupport with ResponseSupport with CommonEndpointSupport with CodeSupport  {
  self: CodeSupport =>
  
  def client: SttpClient
  
  def logger: Logger
  
  protected case class ProxyEndpoint[I, O](
    endpoint: Endpoint[I, ErrorResponse, O, AkkaStreams with WebSockets],
    url: Url,
    handleSuccess: String => Either[ErrorResponse, O],
    handleFailure: String => Either[ErrorResponse, O],
    transformResponseHeaders: Seq[Header] => Seq[Header] = identity,
  )(directive: I => Directive1[client3.Request[Either[String, String], Any]]) {
    def route(implicit encoder: Encoder[O]): MaterializedRoute[I, ErrorResponse, O, AkkaStreams with WebSockets] =
      org.byrde.http.server.MaterializedRoute(endpoint, proxy)
  
    private def proxy(implicit encoder: Encoder[O]): Route =
      AkkaHttpServerInterpreter.toDirective(endpoint).tapply {
        case (input, _) =>
          directive(input) { request =>
            onComplete(client.send(request)) {
              case Success(response) =>
                complete {
                  response.toHttpResponse(
                    transformResponseHeaders,
                    handleSuccess,
                    handleFailure
                  )
                }

              case Failure(ex) =>
                failWith(ex)
            }
          }
      }
  }
  
  protected case class UnitProxyEndpointBuilder[O](
    endpoint: Endpoint[Unit, ErrorResponse, O, AkkaStreams with WebSockets],
    url: Url,
    handleSuccess: String => Either[ErrorResponse, O],
    handleFailure: String => Either[ErrorResponse, O],
    transformRequestHeaders: Seq[Header] => Seq[Header] = identity,
    transformResponseHeaders: Seq[Header] => Seq[Header] = identity,
  ) {
    def handleSuccess(_handleSuccess: String => Either[ErrorResponse, O]): UnitProxyEndpointBuilder[O] =
      copy(handleSuccess = _handleSuccess)
    
    def handleFailure(_handleFailure: String => Either[ErrorResponse, O]): UnitProxyEndpointBuilder[O] =
      copy(handleFailure = _handleFailure)
    
    def withRequestHeaders(transform: Seq[Header] => Seq[Header]): UnitProxyEndpointBuilder[O] =
      copy(transformRequestHeaders = transform)
    
    def withResponseHeaders(transform: Seq[Header] => Seq[Header]): UnitProxyEndpointBuilder[O] =
      copy(transformResponseHeaders = transform)
    
    def materialize: ProxyEndpoint[Unit, O] =
      ProxyEndpoint(
        endpoint,
        url = url,
        handleSuccess = handleSuccess,
        handleFailure = handleFailure,
        transformResponseHeaders = transformResponseHeaders
      )(_ => directive)
    
    private def directive: Directive1[client3.Request[Either[String, String], Any]] =
      extractRequest.map(_.toSttpRequest(url, transformRequestHeaders))
  }
  
  protected case class ProxyEndpointBuilder[I, O](
    endpoint: Endpoint[I, ErrorResponse, O, AkkaStreams with WebSockets],
    url: Url,
    handleSuccess: String => Either[ErrorResponse, O],
    handleFailure: String => Either[ErrorResponse, O],
    transformRequestHeaders: Seq[Header] => Seq[Header] = identity,
    transformResponseHeaders: Seq[Header] => Seq[Header] = identity,
  ) {
    def handleSuccess(_handleSuccess: String => Either[ErrorResponse, O]): ProxyEndpointBuilder[I, O] =
      copy(handleSuccess = _handleSuccess)
    
    def handleFailure(_handleFailure: String => Either[ErrorResponse, O]): ProxyEndpointBuilder[I, O] =
      copy(handleFailure = _handleFailure)
    
    def withRequestHeaders(transform: Seq[Header] => Seq[Header]): ProxyEndpointBuilder[I, O] =
      copy(transformRequestHeaders = transform)
    
    def withResponseHeaders(transform: Seq[Header] => Seq[Header]): ProxyEndpointBuilder[I, O] =
      copy(transformResponseHeaders = transform)
    
    def materialize(implicit serializer: BodySerializer[I]): ProxyEndpoint[I, O] =
      ProxyEndpoint(
        endpoint,
        url = url,
        handleSuccess = handleSuccess,
        handleFailure = handleFailure,
        transformResponseHeaders = transformResponseHeaders
      )(directive)
    
    private def directive(
      input: I
    )(implicit serializer: BodySerializer[I]): Directive1[client3.Request[Either[String, String], Any]] =
      extractRequest.map(_.toSttpRequest(url, Body(input), transformRequestHeaders))
  }
  
  implicit class ProxyEndpoint1[O](endpoint: Endpoint[Unit, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def proxy(url: Url)(implicit decoder: Decoder[O]): UnitProxyEndpointBuilder[O] =
      UnitProxyEndpointBuilder(
        endpoint,
        url,
        decodeProxySuccessResponse(url, _),
        decodeProxyErrorResponse(url, _),
      )
    
    private def decodeProxySuccessResponse(
      url: Url,
      string: String
    )(implicit decoder: Decoder[O]): Either[ErrorResponse, O] =
      parse(string)
        .flatMap(_.as[O])
        .left
        .map { err =>
          logger.warning(s"Proxy: $url, error decoding: $string", err)
          Response.Default(string, errorCode)
        }
    
    private def decodeProxyErrorResponse(url: Url, string: String): Either[ErrorResponse, O] = {
      logger.warning(s"Proxy: $url, error: $string")
      Left(Response.Default(string, errorCode))
    }
  }
  
  implicit class ProxyEndpoint2[I, O](endpoint: Endpoint[I, ErrorResponse, O, AkkaStreams with WebSockets]) {
    def proxy(url: Url)(implicit decoder: Decoder[O]): ProxyEndpointBuilder[I, O] =
      ProxyEndpointBuilder(
        endpoint,
        url,
        decodeProxySuccessResponse(url, _),
        decodeProxyErrorResponse(url, _),
      )
  
    private def decodeProxySuccessResponse(
      url: Url,
      string: String
    )(implicit decoder: Decoder[O]): Either[ErrorResponse, O] =
      parse(string)
        .flatMap(_.as[O])
        .left
        .map { err =>
          logger.warning(s"Proxy: $url, error decoding: $string", err)
          Response.Default(string, errorCode)
        }
  
    private def decodeProxyErrorResponse(url: Url, string: String): Either[ErrorResponse, O] = {
      logger.warning(s"Proxy: $url, error: $string")
      Left(Response.Default(string, errorCode))
    }
  }
  
  implicit class ProxyEndpoint3(endpoint: Endpoint[Unit, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def proxy(url: Url): UnitProxyEndpointBuilder[Response.Default] =
      UnitProxyEndpointBuilder(
        endpoint,
        url,
        decodeProxySuccessResponse,
        decodeProxyErrorResponse(url, _),
      )
  
    private def decodeProxySuccessResponse(string: String): Either[ErrorResponse, Response.Default] =
      Right(Response.Default(string, successCode))
  
    private def decodeProxyErrorResponse(url: Url, string: String): Either[ErrorResponse, Response.Default] = {
      logger.warning(s"Proxy: $url, error: $string")
      Left(Response.Default(string, errorCode))
    }
  }
  
  implicit class ProxyEndpoint4[I](endpoint: Endpoint[I, ErrorResponse, Response.Default, AkkaStreams with WebSockets]) {
    def proxy(url: Url): ProxyEndpointBuilder[I, Response.Default] =
      ProxyEndpointBuilder(
        endpoint,
        url,
        decodeProxySuccessResponse,
        decodeProxyErrorResponse(url, _),
      )
  
    private def decodeProxySuccessResponse(string: String): Either[ErrorResponse, Response.Default] =
      Right(Response.Default(string, successCode))
  
    private def decodeProxyErrorResponse(url: Url, string: String): Either[ErrorResponse, Response.Default] = {
      logger.warning(s"Proxy: $url, error: $string")
      Left(Response.Default(string, errorCode))
    }
  }
}
