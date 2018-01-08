package org.byrde.commons.utils.compressor.conf

import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor

import play.api.Configuration

object JsCompressorConfig {
  lazy val default: JsCompressorConfig =
    apply(Configuration.empty)

  def apply(config: Configuration): JsCompressorConfig =
    apply("optimize", "munge", "semi-colons", config)

  def apply(_optimize: String,
            _munge: String,
            _semiColons: String,
            config: Configuration): JsCompressorConfig = {
    val optimize =
      config.getBoolean(_optimize).getOrElse(true)
    val munge =
      config.getBoolean(_munge).getOrElse(true)
    val semiColons =
      config.getBoolean(_semiColons).getOrElse(true)

    JsCompressorConfig(optimize, munge, semiColons)
  }
}

case class JsCompressorConfig(_optimize: Boolean,
                              _munge: Boolean,
                              _semiColons: Boolean) {
  lazy val compressor: YuiJavaScriptCompressor = {
    val c = new YuiJavaScriptCompressor
    c.setDisableOptimizations(_optimize)
    c.setLineBreak(100)
    c.setNoMunge(_munge)
    c.setPreserveAllSemiColons(_semiColons)
    c
  }
}
