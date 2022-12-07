package org.byrde.logging

import scala.jdk.CollectionConverters._

class FluentLogger(name: String) extends Logger {
  private case class MessageLog(value: String) extends Log {
    override def asMap: Map[String, String] =
      Map("message" -> value)
  }

  private case class ThrowableLog(value: Throwable) extends Log {
    override def asMap: Map[String, String] = {
      import java.io.{PrintWriter, StringWriter}
      val sw = new StringWriter
      value.printStackTrace(new PrintWriter(sw))
      Map("exception" -> sw.toString)
    }
  }

  private val logger = org.fluentd.logger.FluentLogger.getLogger(name)

  override def logDebug(msg: String): Unit =
    logger.log("debug", "message", msg)
  
  override def logDebug(msg: String, extras: Log*): Unit =
    logger.log("debug", (MessageLog(msg) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logInfo(msg: String): Unit =
    logger.log("info", MessageLog(msg).asMap.asInstanceOf[Map[String, AnyRef]].asJava)
  
  override def logInfo(msg: String, extras: Log*): Unit =
    logger.log("info", (MessageLog(msg) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logWarning(msg: String): Unit =
    logger.log("warning", MessageLog(msg).asMap.asInstanceOf[Map[String, AnyRef]].asJava)
  
  override def logWarning(msg: String, extras: Log*): Unit =
    logger.log("warning", (MessageLog(msg) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logWarning(msg: String, cause: Throwable): Unit =
    logger.log("warning", (MessageLog(msg) ++ ThrowableLog(cause)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logWarning(msg: String, cause: Throwable, extras: Log*): Unit =
    logger.log("warning", (MessageLog(msg) ++ ThrowableLog(cause) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logError(msg: String): Unit =
    logger.log("error", MessageLog(msg).asMap.asInstanceOf[Map[String, AnyRef]].asJava)
  
  override def logError(msg: String, extras: Log*): Unit =
    logger.log("error", (MessageLog(msg) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logError(msg: String, cause: Throwable): Unit =
    logger.log("error", (MessageLog(msg) ++ ThrowableLog(cause)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)

  override def logError(msg: String, cause: Throwable, extras: Log*): Unit =
    logger.log("error", (MessageLog(msg) ++ ThrowableLog(cause) ++ extras.foldLeft(Log.empty)(_ ++ _)).asMap.asInstanceOf[Map[String, AnyRef]].asJava)
}