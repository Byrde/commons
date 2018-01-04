package org.byrde.commons.utils.compressor.impl

import akka.stream.Materializer

import com.googlecode.htmlcompressor.compressor.YuiCssCompressor

import org.byrde.commons.utils.compressor.ResultCompressor
import org.byrde.commons.utils.compressor.conf.CssCompressorConfig

import play.api.http.MimeTypes

import scala.concurrent.ExecutionContext

case class CssResultCompressor(implicit ec: ExecutionContext, mat: Materializer) extends ResultCompressor[YuiCssCompressor]{
  override lazy val compressor: YuiCssCompressor =
    CssCompressorConfig().compressor

  override def isCompressible(result: _root_.play.api.mvc.Result): Boolean =  {
    val contentType = result.body.contentType.exists {
      _.contains(MimeTypes.CSS)
    }
    super.isCompressible(result) && contentType
  }
}
