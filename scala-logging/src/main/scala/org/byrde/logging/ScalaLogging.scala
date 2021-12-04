package org.byrde.logging

import com.typesafe.scalalogging.StrictLogging
import net.logstash.logback.marker.Markers.appendEntries

import scala.jdk.CollectionConverters._

trait ScalaLogging extends StrictLogging {
  def logDebug(msg: String): Unit =
    logger.debug(msg)
  
  def logDebug(msg: String, extras: Log): Unit =
    logger.debug(msg, appendEntries(extras.asMap.asJava))
  
  def logInfo(msg: String): Unit =
    logger.info(msg)
  
  def logInfo(msg: String, extras: Log): Unit =
    logger.info(msg, appendEntries(extras.asMap.asJava))
  
  def logWarning(msg: String): Unit =
    logger.warn(msg)
  
  def logWarning(msg: String, extras: Log): Unit =
    logger.warn(msg, appendEntries(extras.asMap.asJava))
  
  def logWarning(msg: String, cause: Throwable): Unit =
    logger.warn(msg, cause)
  
  def logError(msg: String): Unit =
    logger.error(msg)
  
  def logError(msg: String, extras: Log): Unit =
    logger.error(msg, appendEntries(extras.asMap.asJava))
  
  def logError(msg: String, cause: Throwable): Unit =
    logger.error(msg, cause)
}
