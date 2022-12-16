package org.byrde.logging

import java.io.{ PrintWriter, StringWriter }

import scala.jdk.CollectionConverters._
import scala.util.chaining._

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments._

class ScalaLogger(name: String, includeSensitiveLogs: Boolean = false) extends Logger {
  private case class StackTraceLog(value: Throwable) extends Log {
    override def logs: Seq[(String, String)] = {
      val sw = new StringWriter
      value.printStackTrace(new PrintWriter(sw))
      Seq("stack_trace" -> sw.toString)
    }

    override def sensitiveLogs: Seq[(String, String)] = Seq.empty
  }

  private val logger = com.typesafe.scalalogging.Logger(name)

  override def logDebug(msg: String): Unit = logger.debug(msg)

  override def logDebug(msg: String, extras: Log*): Unit = logger.debug(msg, buildEntries(extras))

  override def logInfo(msg: String): Unit = logger.info(msg)

  override def logInfo(msg: String, extras: Log*): Unit = logger.info(msg, buildEntries(extras))

  override def logWarning(msg: String): Unit = logger.warn(msg)

  override def logWarning(msg: String, extras: Log*): Unit = logger.warn(msg, buildEntries(extras))

  override def logWarning(msg: String, cause: Throwable): Unit = logger.warn(msg, cause)

  override def logWarning(msg: String, cause: Throwable, extras: Log*): Unit =
    logger.warn(s"$msg", buildEntries(extras :+ StackTraceLog(cause)))

  override def logError(msg: String): Unit = logger.error(msg)

  override def logError(msg: String, extras: Log*): Unit = logger.error(msg, buildEntries(extras))

  override def logError(msg: String, cause: Throwable): Unit = logger.error(msg, cause)

  override def logError(msg: String, cause: Throwable, extras: Log*): Unit =
    logger.error(s"$msg", buildEntries(extras :+ StackTraceLog(cause)))

  private def buildEntries(logs: Seq[Log]): StructuredArgument =
    logs
      .foldLeft(Log.empty)(_ ++ _)
      .pipe {
        case log if includeSensitiveLogs =>
          entries((log.logs ++ log.sensitiveLogs).toMap.asJava)

        case log =>
          entries(log.logs.toMap.asJava)
      }
}
