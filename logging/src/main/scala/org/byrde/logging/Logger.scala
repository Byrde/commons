package org.byrde.logging

trait Logger {

  def debug(msg: String): Unit
  
  def debug(msg: String, extras: Log): Unit

  def info(msg: String): Unit
  
  def info(msg: String, extras: Log): Unit

  def warning(msg: String): Unit
  
  def warning(msg: String, extras: Log): Unit
  
  def warning(msg: String, cause: Throwable): Unit

  def error(msg: String): Unit
  
  def error(msg: String, extras: Log): Unit

  def error(msg: String, cause: Throwable): Unit
}
