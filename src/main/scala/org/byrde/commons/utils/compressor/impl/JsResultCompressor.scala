package org.byrde.commons.utils.compressor.impl

import akka.stream.Materializer

import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor

import org.byrde.commons.utils.compressor.ResultCompressor
import org.byrde.commons.utils.compressor.conf.JsCompressorConfig

import play.api.http.MimeTypes

import scala.concurrent.ExecutionContext

case class JsResultCompressor(jsConfig: JsCompressorConfig)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends ResultCompressor[YuiJavaScriptCompressor] {
  override lazy val compressor: YuiJavaScriptCompressor =
    jsConfig.compressor

  override def isCompressible(result: _root_.play.api.mvc.Result): Boolean = {
    val contentType = result.body.contentType.exists {
      _.contains(MimeTypes.JAVASCRIPT)
    }
    super.isCompressible(result) && contentType
  }
}
