package org.byrde.logging

case class WrappedLogger(private val logger: Logger) {
  trait MixIn extends Logger {
    override def logDebug(msg: String): Unit =
      logger.logDebug(msg)
  
    override def logDebug(msg: String, extras: Log*): Unit =
      logger.logDebug(msg, extras: _*)
  
    override def logInfo(msg: String): Unit =
      logger.logInfo(msg)
  
    override def logInfo(msg: String, extras: Log*): Unit =
      logger.logInfo(msg, extras: _*)
  
    override def logWarning(msg: String): Unit =
      logger.logWarning(msg)
  
    override def logWarning(msg: String, extras: Log*): Unit =
      logger.logWarning(msg, extras: _*)
  
    override def logError(msg: String): Unit =
      logger.logError(msg)
  
    override def logError(msg: String, extras: Log*): Unit =
      logger.logError(msg, extras: _*)
  
    override def logError(msg: String, cause: Throwable): Unit =
      logger.logError(msg, cause)

    override def logError(msg: String, cause: Throwable, extras: Log*): Unit =
      logger.logError(msg, cause, extras: _*)
  }
}
