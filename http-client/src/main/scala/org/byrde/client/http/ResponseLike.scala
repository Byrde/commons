package org.byrde.client.http

import org.byrde.uri.Url

trait ResponseLike {

  def url: Url

  def method: Method

  def requestHeaders: Headers

  def responseHeaders: Headers

  def status: Status

  def body: Body

}

object ResponseLike {

  def apply[T <: RequestLike](request: T): ResponseLike =
    new ResponseLike {
      override def url: Url =
        request.url

      override def method: Method =
        request.method

      override def requestHeaders: Headers =
        request.headers

      override def responseHeaders: Headers =
        Map.empty

      override def status: Status =
        -1

      override def body: Body =
        "N/A"
    }

}