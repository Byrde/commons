package org.byrde.commons.services

import play.api.libs.ws.ahc.{CurlFormat, StandaloneAhcWSRequest}
import play.api.libs.ws.{WSRequestExecutor, WSRequestFilter}

class AhcCurlRequestFilter(log: String => Unit) extends WSRequestFilter with CurlFormat {
  def apply(executor: WSRequestExecutor): WSRequestExecutor = {
    WSRequestExecutor { request =>
      log(toCurl(request.asInstanceOf[StandaloneAhcWSRequest]))
      executor(request)
    }
  }
}