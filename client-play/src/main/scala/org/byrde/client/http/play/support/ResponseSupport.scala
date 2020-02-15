package org.byrde.client.http.play.support

import play.api.libs.ws.StandaloneWSResponse

import org.byrde.client.http._
import org.byrde.uri.Url

trait ResponseSupport {

  implicit class StandaloneWSResponse2ResponseLike(value: StandaloneWSResponse) {
    def toResponse[T <: RequestLike](request: T): ResponseLike =
      new ResponseLike {
        override def url: Url =
          request.url

        override def method: Method =
          request.method

        override def requestHeaders: Headers =
          request.headers

        override def responseHeaders: Headers =
          value.headers.view.mapValues(_.mkString(", ")).toMap

        override def status: Status =
          value.status

        override def body: Body =
          value.body
      }
  }

}
