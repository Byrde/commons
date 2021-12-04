package org.byrde.http.server

import org.byrde.logging.{Log, Logger}

class TestLogger extends Logger {
  override def logDebug(msg: String): Unit = ()
  
  override def logDebug(msg: String, extras: Log): Unit = ()
  
  override def logInfo(msg: String): Unit = ()
  
  override def logInfo(msg: String, extras: Log): Unit = ()
  
  override def logWarning(msg: String): Unit = ()
  
  override def logWarning(msg: String, extras: Log): Unit = ()
  
  override def logWarning(msg: String, cause: Throwable): Unit = ()
  
  override def logError(msg: String): Unit = ()
  
  override def logError(msg: String, extras: Log): Unit = ()
  
  override def logError(msg: String, cause: Throwable): Unit = ()
}
