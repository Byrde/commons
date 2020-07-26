package org.byrde.logging

import net.logstash.logback.marker.Markers._
import scala.jdk.CollectionConverters._

class ScalaLogger(name: String) extends Logger {

  private val logger = com.typesafe.scalalogging.Logger(name)

  override def debug(msg: String): Unit =
    logger.debug(msg)
  
  override def debug(msg: String, extras: Log): Unit =
    logger.debug(msg, appendEntries(extras.asMap.asJava))

  override def info(msg: String): Unit =
    logger.info(msg)
  
  override def info(msg: String, extras: Log): Unit =
    logger.info(msg, appendEntries(extras.asMap.asJava))

  override def warning(msg: String): Unit =
    logger.warn(msg)
  
  override def warning(msg: String, extras: Log): Unit =
    logger.warn(msg, appendEntries(extras.asMap.asJava))

  override def error(msg: String): Unit =
    logger.error(msg)
  
  override def error(msg: String, extras: Log): Unit =
    logger.error(msg, appendEntries(extras.asMap.asJava))

  override def error(msg: String, cause: Throwable): Unit =
    logger.error(msg, cause)
  
  override def error(msg: String, extras: Log, cause: Throwable): Unit =
    logger.error(msg, cause, appendEntries(extras.asMap.asJava))

}