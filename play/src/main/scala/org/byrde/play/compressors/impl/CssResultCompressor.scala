package org.byrde.play.compressors.impl

import akka.stream.Materializer

import com.googlecode.htmlcompressor.compressor.YuiCssCompressor

import org.byrde.play.compressors.ResultCompressor
import org.byrde.play.compressors.conf.CssCompressorConfig

import play.api.http.MimeTypes

case class CssResultCompressor()(implicit mat: Materializer)
  extends ResultCompressor[YuiCssCompressor] {
  override lazy val compressor: YuiCssCompressor =
    CssCompressorConfig().compressor

  override def isCompressible(result: _root_.play.api.mvc.Result): Boolean = {
    val contentType =
      result
        .body
        .contentType
        .exists(_.contains(MimeTypes.CSS))

    super.isCompressible(result) && contentType
  }
}
