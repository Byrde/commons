package org.byrde.akka.http.scaladsl.server.directives

import akka.http.scaladsl.model._

import scala.collection.immutable

/**
 * Completely substituable for [[HttpRequestWithEntity<None.type>]]
 */
class HttpRequestWithNoEntity(override val request: HttpRequest) extends HttpRequestWithEntity(None, request)

object HttpRequestWithNoEntity {
  
  def apply(
    method: HttpMethod = HttpMethods.GET,
    uri: Uri = Uri./,
    headers: immutable.Seq[HttpHeader] = Nil,
    entity: RequestEntity = HttpEntity.Empty,
    protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`
  ): HttpRequestWithNoEntity =
    new HttpRequestWithNoEntity(HttpRequest(method, uri, headers, entity, protocol))
  
}