package org.byrde.logging

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}

trait Logging {
  
  private lazy val printer = Printer.noSpaces.copy(dropNullValues = true)
  
  def logger: Logger
  
  private [logging] case class Message(message: String)

  def debug[T](msg: T)(implicit encoder: Encoder[T]): Unit =
    info(msg.asJson)
  
  def debug[T](msg: String): Unit =
    info(Message(msg).asJson)
  
  def debug[T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    info(Message(msg).asJson deepMerge extra.asJson)

  def debug(msg: Json): Unit =
    logger.debug(msg.printWith(printer))

  def info[T](msg: T)(implicit encoder: Encoder[T]): Unit =
    info(msg.asJson)
  
  def info[T](msg: String): Unit =
    info(Message(msg).asJson)
  
  def info[T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    info(Message(msg).asJson deepMerge extra.asJson)

  def info(msg: Json): Unit =
    logger.info(msg.printWith(printer))

  def warning[T](msg: T)(implicit encoder: Encoder[T]): Unit =
    warning(msg.asJson)
  
  def warning[T](msg: String): Unit =
    warning(Message(msg).asJson)
  
  def warning[T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    warning(Message(msg).asJson deepMerge extra.asJson)

  def warning(msg: Json): Unit =
    logger.warning(msg.printWith(printer))

  def error[T](msg: T)(implicit encoder: Encoder[T]): Unit =
    error(msg.asJson)
  
  def error[T](msg: String): Unit =
    error(Message(msg).asJson)
  
  def error[T](msg: String, extra: T)(implicit encoder: Encoder[T]): Unit =
    error(Message(msg).asJson deepMerge extra.asJson)

  def error(msg: Json): Unit =
    logger.error(msg.printWith(printer))

  def error[T](msg: T, ex: Throwable)(implicit encoder: Encoder[T]): Unit =
    error(msg.asJson, ex)
  
  def error[T](msg: String, ex: Throwable): Unit =
    error(Message(msg).asJson, ex)
  
  def error[T](msg: String, extra: T, ex: Throwable)(implicit encoder: Encoder[T]): Unit =
    error(Message(msg).asJson deepMerge extra.asJson, ex)

  def error(msg: Json, ex: Throwable): Unit =
    logger.error(msg.printWith(printer), ex)

}