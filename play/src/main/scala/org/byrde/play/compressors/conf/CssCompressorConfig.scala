package org.byrde.play.compressors.conf

import com.googlecode.htmlcompressor.compressor.YuiCssCompressor

case class CssCompressorConfig() {
  lazy val compressor: YuiCssCompressor = {
    val compressor = new YuiCssCompressor()
    compressor.setLineBreak(100)
    compressor
  }
}
