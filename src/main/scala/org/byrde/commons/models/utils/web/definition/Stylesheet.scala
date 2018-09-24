package org.byrde.commons.models.utils.web.definition

import scala.xml.Elem

trait Stylesheet {
  def markup: Elem
}

case class StylesheetInline(private val css: String) extends Stylesheet {
  def markup: Elem =
    <style media="text/css">{ css }</style>
}