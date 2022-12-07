package org.byrde.logging

trait Logger {
  def logDebug(msg: String): Unit
  
  def logDebug(msg: String, extras: Log*): Unit

  def logInfo(msg: String): Unit
  
  def logInfo(msg: String, extras: Log*): Unit

  def logWarning(msg: String): Unit
  
  def logWarning(msg: String, extras: Log*): Unit

  def logWarning(msg: String, cause: Throwable): Unit

  def logWarning(msg: String, cause: Throwable, extras: Log*): Unit

  def logError(msg: String): Unit
  
  def logError(msg: String, extras: Log*): Unit

  def logError(msg: String, cause: Throwable): Unit

  def logError(msg: String, cause: Throwable, extras: Log*): Unit
}
