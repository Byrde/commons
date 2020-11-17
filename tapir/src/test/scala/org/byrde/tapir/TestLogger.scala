package org.byrde.tapir

import org.byrde.logging.{Log, Logger}

class TestLogger extends Logger {
  override def debug(msg: String): Unit = ()
  
  override def debug(msg: String, extras: Log): Unit = ()
  
  override def info(msg: String): Unit = ()
  
  override def info(msg: String, extras: Log): Unit = ()
  
  override def warning(msg: String): Unit = ()
  
  override def warning(msg: String, extras: Log): Unit = ()
  
  override def warning(msg: String, cause: Throwable): Unit = ()
  
  override def error(msg: String): Unit = ()
  
  override def error(msg: String, extras: Log): Unit = ()
  
  override def error(msg: String, cause: Throwable): Unit = ()
}
