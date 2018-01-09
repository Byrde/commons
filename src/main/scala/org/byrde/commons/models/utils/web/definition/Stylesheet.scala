package org.byrde.commons.models.utils.web.definition

import scala.xml.Elem

/**
  * Created by martin.allaire 2016.
  */
trait Stylesheet {
  def markup: Elem
}

case class StylesheetInline(private val css: String) extends Stylesheet {
  def markup = {
    <style media="text/css">{ css }</style>
  }
}