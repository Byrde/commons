
/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package org.byrde.play.utils

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.libs.ws.{EmptyBody, _}
import play.shaded.ahc.org.asynchttpclient.util.HttpUtils

/**
 * Logs StandaloneWSRequest and pulls information into Curl format to an SLF4J logger.
 *
 * @param logger an SLF4J logger
 *
 * @see <a href="https://curl.haxx.se/">https://curl.haxx.se/</a>
 */
case class AhcCurlRequestLogger(logger: ApplicationLogger) {
  def apply(request: StandaloneWSRequest): StandaloneWSRequest = {
    logger.info(toCurl(request))
    request
  }

  private def toCurl(request: StandaloneWSRequest): String = {
    val b = new StringBuilder("curl \\\n")

    // verbose, since it's a fair bet this is for debugging
    b.append("  --verbose")
    b.append(" \\\n")

    // method
    b.append(s"  --request ${request.method}")
    b.append(" \\\n")

    //authentication
    request.auth match {
      case Some((userName, password, WSAuthScheme.BASIC)) =>
        val encodedPassword = Base64.getUrlEncoder.encodeToString(s"$userName:$password".getBytes(StandardCharsets.US_ASCII))
        b.append(s"""  --header 'Authorization: Basic ${quote(encodedPassword)}'""")
        b.append(" \\\n")
      case _ => ()
    }

    // headers
    request.headers.foreach {
      case (k, values) =>
        values.foreach { v =>
          b.append(s"  --header '${quote(k)}: ${quote(v)}'")
          b.append(" \\\n")
        }
    }

    // cookies
    request.cookies.foreach { cookie =>
      b.append(s"""  --cookie '${cookie.name}=${cookie.value}'""")
      b.append(" \\\n")
    }

    // body (note that this has only been checked for text, not binary)
    request.body match {
      case InMemoryBody(byteString) =>
        val charset = findCharset(request)
        val bodyString = byteString.decodeString(charset)
        // XXX Need to escape any quotes within the body of the string.
        b.append(s"  --data '${quote(bodyString)}'")
        b.append(" \\\n")
      case EmptyBody => // Do nothing.
      case other =>
        throw new UnsupportedOperationException("Unsupported body type " + other.getClass)
    }

    // pull out some underlying values from the request.  This creates a new Request
    // but should be harmless.
    request.proxyServer.map { proxyServer =>
      b.append(s"  --proxy ${proxyServer.host}:${proxyServer.port}")
      b.append(" \\\n")
    }

    // url
    b.append(s"  '${quote(request.url)}'")

    val curlOptions = b.toString()
    curlOptions
  }

  private def findCharset(request: StandaloneWSRequest): String =
    request.contentType.map { ct =>
      Option(HttpUtils.extractContentTypeCharsetAttribute(ct)).getOrElse {
        StandardCharsets.UTF_8
      }.name()
    }.getOrElse(HttpUtils.extractContentTypeCharsetAttribute("UTF-8").name())

  private def quote(unsafe: String): String = unsafe.replace("'", "'\\''")
}