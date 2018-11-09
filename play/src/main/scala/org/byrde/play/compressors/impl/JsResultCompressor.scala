package org.byrde.play.compressors.impl

import akka.stream.Materializer

import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor

import org.byrde.play.compressors.ResultCompressor
import org.byrde.play.compressors.conf.JsCompressorConfig

import play.api.http.MimeTypes

case class JsResultCompressor(jsConfig: JsCompressorConfig)(implicit mat: Materializer)
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
