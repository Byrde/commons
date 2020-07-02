package org.byrde.client.http

import scala.concurrent.Future

trait HttpExecutor[R, I, A] {
  
  def executor: HttpExecutor.Service[R, I, A]
  
}

object HttpExecutor {
  
  trait Service[R, I, A] {
    def execute(request: I): Future[Either[HttpClientError, A]]
  }
  
}