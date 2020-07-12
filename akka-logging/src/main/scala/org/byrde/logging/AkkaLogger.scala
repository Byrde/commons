package org.byrde.logging

import akka.actor.ActorSystem
import akka.event.Logging

class AkkaLogger(name: String)(implicit system: ActorSystem) extends Logger {

  private val logger = Logging(system, name)

  override def debug(msg: String): Unit =
    logger.debug(msg)

  override def info(msg: String): Unit =
    logger.info(msg)

  override def warning(msg: String): Unit =
    logger.warning(msg)

  override def error(msg: String): Unit =
    logger.error(msg)

  override def error(msg: String, cause: Throwable): Unit =
    logger.error(s"$msg: {}", cause)

}