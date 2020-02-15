package org.byrde.client.http.play.support

import play.api.libs.ws.StandaloneWSRequest

import org.byrde.client.http.play.PlayRequest
import org.byrde.client.http.{Headers, Method}
import org.byrde.uri.Url

trait RequestSupport {

  implicit class StandaloneWSResponse2ResponseLike(value: StandaloneWSRequest) {
    def toRequest: PlayRequest =
      new PlayRequest {
        override def url: Url =
          Url.fromString(value.url)

        override def method: Method =
          value.method

        override def headers: Headers =
          value.headers.view.mapValues(_.mkString(", ")).toMap
      }
  }

}