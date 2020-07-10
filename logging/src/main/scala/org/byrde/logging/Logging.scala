package org.byrde.logging

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}

trait Logging {
  
  def logger: Logger
  
  private [logging] case class Message(message: String)

  def debug[R <: Logger, T](msg: T)(implicit encoder: Encoder[T], logger: Logger): Unit =
    info(msg.asJson)
  
  def debug[R <: Logger, T](msg: String): Unit =
    info(Message(msg).asJson)
  
  def debug[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    info(Message(msg).asJson deepMerge extra.asJson)

  def debug[R <: Logger](msg: Json): Unit =
    logger.debug(msg.printWith(Printer.noSpaces))

  def info[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): Unit =
    info(msg.asJson)
  
  def info[R <: Logger, T](msg: String): Unit =
    info(Message(msg).asJson)
  
  def info[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    info(Message(msg).asJson deepMerge extra.asJson)

  def info[R <: Logger](msg: Json): Unit =
    logger.info(msg.printWith(Printer.noSpaces))

  def warning[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): Unit =
    warning(msg.asJson)
  
  def warning[R <: Logger, T](msg: String): Unit =
    warning(Message(msg).asJson)
  
  def warning[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    warning(Message(msg).asJson deepMerge extra.asJson)

  def warning[R <: Logger](msg: Json): Unit =
    logger.warning(msg.printWith(Printer.noSpaces))

  def error[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): Unit =
    error(msg.asJson)
  
  def error[R <: Logger, T](msg: String): Unit =
    error(Message(msg).asJson)
  
  def error[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    error(Message(msg).asJson deepMerge extra.asJson)

  def error[R <: Logger](msg: Json): Unit =
    logger.error(msg.printWith(Printer.noSpaces))

  def error[R <: Logger, T](msg: T, ex: Throwable)(implicit encoder: Encoder[T]): Unit =
    error(msg.asJson, ex)
  
  def error[R <: Logger, T](msg: String, ex: Throwable): Unit =
    error(Message(msg).asJson, ex)
  
  def error[R <: Logger, T](msg: String, extra: T, ex: Throwable)(implicit encoder: Encoder[T]): Unit =
    error(Message(msg).asJson deepMerge extra.asJson, ex)

  def error[R <: Logger](msg: Json, ex: Throwable): Unit =
    logger.error(msg.printWith(Printer.noSpaces), ex)

}