package org.byrde.logging

class PlayLogger(name: String) extends Logger {

  private val logger = play.api.Logger(name)

  override def debug(msg: String): Unit =
    logger.debug(msg)

  override def info(msg: String): Unit =
    logger.info(msg)

  override def warning(msg: String): Unit =
    logger.warn(msg)

  override def error(msg: String): Unit =
    logger.error(msg)

  override def error(msg: String, cause: Throwable): Unit =
    logger.error(msg, cause)

}