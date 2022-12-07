package org.byrde.logging

import net.logstash.logback.argument.StructuredArguments._

import java.io.{PrintWriter, StringWriter}

import scala.jdk.CollectionConverters._

class ScalaLogger(name: String) extends Logger {
  private case class StackTraceLog(value: Throwable) extends Log {
    override def asMap: Map[String, String] = {
      val sw = new StringWriter
      value.printStackTrace(new PrintWriter(sw))
      Map("stack_trace" -> sw.toString)
    }
  }

  private val logger = com.typesafe.scalalogging.Logger(name)

  override def logDebug(msg: String): Unit =
    logger.debug(msg)

  override def logDebug(msg: String, extras: Log*): Unit =
    logger.debug(msg, entries(extras.foldLeft(Log.empty)(_ ++ _).asMap.asJava))

  override def logInfo(msg: String): Unit =
    logger.info(msg)

  override def logInfo(msg: String, extras: Log*): Unit =
    logger.info(msg, entries(extras.foldLeft(Log.empty)(_ ++ _).asMap.asJava))

  override def logWarning(msg: String): Unit =
    logger.warn(msg)

  override def logWarning(msg: String, extras: Log*): Unit =
    logger.warn(msg, entries(extras.foldLeft(Log.empty)(_ ++ _).asMap.asJava))

  override def logWarning(msg: String, cause: Throwable): Unit =
    logger.warn(msg, cause)

  override def logWarning(msg: String, cause: Throwable, extras: Log*): Unit =
    logger.warn(s"$msg", entries((extras :+ StackTraceLog(cause)).foldLeft(Log.empty)(_ ++ _).asMap.asJava))

  override def logError(msg: String): Unit =
    logger.error(msg)

  override def logError(msg: String, extras: Log*): Unit =
    logger.error(msg, entries(extras.foldLeft(Log.empty)(_ ++ _).asMap.asJava))

  override def logError(msg: String, cause: Throwable): Unit =
    logger.error(msg, cause)

  override def logError(msg: String, cause: Throwable, extras: Log*): Unit = {
    logger.error(s"$msg", entries((extras :+ StackTraceLog(cause)).foldLeft(Log.empty)(_ ++ _).asMap.asJava))
  }
}