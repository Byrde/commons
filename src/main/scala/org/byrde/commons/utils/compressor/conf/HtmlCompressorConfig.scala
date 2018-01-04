package org.byrde.commons.utils.compressor.conf

import com.googlecode.htmlcompressor.compressor.HtmlCompressor

import play.api.Configuration

case class HtmlCompressorConfig(
  _lineBreak: Boolean,
  _comments: Boolean,
  _intSpace: Boolean,
  _rHttp: Boolean,
  _rHttps: Boolean) {
  lazy val compressor: HtmlCompressor = {
    val c = new HtmlCompressor
    c.setPreserveLineBreaks(_lineBreak)
    c.setRemoveComments(_comments)
    c.setRemoveIntertagSpaces(_intSpace)
    c.setRemoveHttpProtocol(_rHttp)
    c.setRemoveHttpsProtocol(_rHttps)
    c
  }
}

object HtmlCompressorConfig {
  lazy val default: HtmlCompressorConfig =
    apply(Configuration.empty)

  def apply(config: Configuration): HtmlCompressorConfig =
    apply("lineBreak", "comments", "integerSpace", "rHttpProtocol",  "rHttpsProtocol", config)

  def apply(
    _lineBreak: String,
    _comments: String,
    _intSpace: String,
    _rHttp: String,
    _rHttps: String,
    config: Configuration): HtmlCompressorConfig = {
    val lineBreak =
      config.getBoolean(_lineBreak).getOrElse(true)
    val comments =
      config.getBoolean(_comments).getOrElse(false)
    val intSpace =
      config.getBoolean(_intSpace).getOrElse(false)
    val rHttp =
      config.getBoolean(_rHttp).getOrElse(false)
    val rHttps =
      config.getBoolean(_rHttps).getOrElse(false)

    HtmlCompressorConfig(lineBreak, comments, intSpace, rHttp, rHttps)
  }
}

