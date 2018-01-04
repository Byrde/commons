package org.byrde.commons.controllers.actions.compress

import akka.stream.Materializer

import org.byrde.commons.utils.compressor.GzipResult
import org.byrde.commons.utils.compressor.conf.{HtmlCompressorConfig, JsCompressorConfig}
import org.byrde.commons.utils.compressor.impl.{CssResultCompressor, HtmlResultCompressor, JsResultCompressor}

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CompressAction (
  _htmlConfig: Option[HtmlCompressorConfig] = None,
  _jsConfig: Option[JsCompressorConfig] = None)(override implicit val executionContext: ExecutionContext, implicit val mat: Materializer) extends ActionBuilder[Request] {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    val htmlConfig =
      _htmlConfig.getOrElse(HtmlCompressorConfig.default)
    val jsConfig =
      _jsConfig.getOrElse(JsCompressorConfig.default)

    block(request).flatMap{ result =>
      val htmlCompressor =
        HtmlResultCompressor(htmlConfig)
      val cssCompressor =
        CssResultCompressor()
      val jsCompressor =
        JsResultCompressor(jsConfig)

      val compressedResultFuture = {
        if (htmlCompressor.isCompressible(result))
          htmlCompressor.compressResult(result)
        else if (cssCompressor.isCompressible(result))
          cssCompressor.compressResult(result)
        else if (jsCompressor.isCompressible(result))
          jsCompressor.compressResult(result)
        else
          Future.successful(result)
      }

      compressedResultFuture flatMap { compressedResult =>
        val gzipCompressor =
          new GzipResult

        if (gzipCompressor.isCompressible(request)) {
          gzipCompressor.handleResult(request, compressedResult)
        } else {
          Future(compressedResult)
        }
      }
    }
  }
}
