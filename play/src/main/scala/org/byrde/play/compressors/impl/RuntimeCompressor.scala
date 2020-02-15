package org.byrde.play.compressors.impl

import java.io._
import java.util.zip.GZIPOutputStream

import org.byrde.play.compressors.conf.{CssCompressorConfig, HtmlCompressorConfig, JsCompressorConfig}
import org.apache.commons.io.{FilenameUtils, IOUtils}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}
import scala.util.Using

case class RuntimeCompressor(_htmlConfig: Option[HtmlCompressorConfig] = None, _jsConfig: Option[JsCompressorConfig] = None)(implicit ec: ExecutionContext) {
  //Expensive operation, usually only put through compress processor if also caching results
  def compress(orig: File): File = {
    val origFileName =
      orig.getName

    val origFileExt =
      origFileName.substring(origFileName.lastIndexOf(".") + 1)

    if (origFileExt != "gz") {
      val is =
        new FileInputStream(orig)

      val file =
        new File(s"$origFileName")

      commonCompress(is, file)
    } else orig
  }

  private def commonCompress(is: InputStream, target: File) = {
    val htmlConfig =
      _htmlConfig.getOrElse(HtmlCompressorConfig.default)

    val jsConfig =
      _jsConfig.getOrElse(JsCompressorConfig.default)

    val zip =
      new GZIPOutputStream(new FileOutputStream(target), 1024, true)

    val ow =
      new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"), 1024)

    val ext =
      FilenameUtils.getExtension(target.getName)

    if (ext == "html") {
      Future {
        htmlConfig.compressor.compress(IOUtils.toString(is, "UTF-8"))
      }.map { html =>
        ow.write(html)
      }
    } else if (ext == "css") {
      Future {
        CssCompressorConfig().compressor.compress(IOUtils.toString(is, "UTF-8"))
      }.map { html =>
        ow.write(html)
      }
    } else if (ext == "js") {
      Future {
        jsConfig.compressor.compress(IOUtils.toString(is, "UTF-8"))
      }.map { html =>
        ow.write(html)
      }
    } else {
      Using(Source.fromInputStream(is)(Codec.UTF8)) { source =>
        source.getLines.foreach(ow.write)
      }
    }

    target.renameTo(new File(s"${target.getName}.gz"))
    target
  }
}
