package org.byrde.commons.utils.compressor.impl

import akka.stream.Materializer

import com.googlecode.htmlcompressor.compressor.HtmlCompressor

import org.byrde.commons.utils.compressor.ResultCompressor
import org.byrde.commons.utils.compressor.conf.HtmlCompressorConfig

import play.api.http.MimeTypes

case class HtmlResultCompressor(htmlConfig: HtmlCompressorConfig)(implicit mat: Materializer)
  extends ResultCompressor[HtmlCompressor] {
  override lazy val compressor: HtmlCompressor =
    htmlConfig.compressor

  override def isCompressible(result: _root_.play.api.mvc.Result): Boolean = {
    val contentType = result.body.contentType.exists {
      _.contains(MimeTypes.HTML)
    }
    super.isCompressible(result) && contentType
  }
}
