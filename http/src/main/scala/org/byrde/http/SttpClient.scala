package org.byrde.http

import sttp.client3
import sttp.client3.Request

import scala.concurrent.Future

trait SttpClient {
  def send(request: Request[Either[String, String], Any]): Future[client3.Response[Either[String, String]]]
}
