package org.byrde.logging

trait LoggingFormatter[-T] {
  def format(elem: T): String
}
