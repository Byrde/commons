package org.byrde.logging

import io.circe.{Encoder, Json, Printer}
import io.circe.generic.auto._
import io.circe.syntax._

import zio.{RIO, ZIO}

trait Logging {

  private [logging] case class Message(message: String)

  def debug[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    info(msg.asJson)
  
  def debug[R <: Logger, T](msg: String): RIO[R, Unit] =
    info(Message(msg).asJson)
  
  def debug[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    info(Message(msg).asJson deepMerge extra.asJson)

  def debug[R <: Logger](msg: Json): RIO[R, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- ZIO(env.debug(msg.printWith(Printer.noSpaces)))
    } yield result

  def info[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    info(msg.asJson)
  
  def info[R <: Logger, T](msg: String): RIO[R, Unit] =
    info(Message(msg).asJson)
  
  def info[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    info(Message(msg).asJson deepMerge extra.asJson)

  def info[R <: Logger](msg: Json): RIO[R, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- ZIO(env.info(msg.printWith(Printer.noSpaces)))
    } yield result

  def warning[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    warning(msg.asJson)
  
  def warning[R <: Logger, T](msg: String): RIO[R, Unit] =
    warning(Message(msg).asJson)
  
  def warning[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    warning(Message(msg).asJson deepMerge extra.asJson)

  def warning[R <: Logger](msg: Json): RIO[R, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- ZIO(env.warning(msg.printWith(Printer.noSpaces)))
    } yield result

  def error[R <: Logger, T](msg: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    error(msg.asJson)
  
  def error[R <: Logger, T](msg: String): RIO[R, Unit] =
    error(Message(msg).asJson)
  
  def error[R <: Logger, T](msg: String, extra: T)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    error(Message(msg).asJson deepMerge extra.asJson)

  def error[R <: Logger](msg: Json): RIO[R, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- ZIO(env.error(msg.printWith(Printer.noSpaces)))
    } yield result

  def error[R <: Logger, T](msg: T, ex: Throwable)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    error(msg.asJson, ex)
  
  def error[R <: Logger, T](msg: String, ex: Throwable): RIO[R, Unit] =
    error(Message(msg).asJson, ex)
  
  def error[R <: Logger, T](msg: String, extra: T, ex: Throwable)(implicit encoder: Encoder[T]): RIO[R, Unit] =
    error(Message(msg).asJson deepMerge extra.asJson, ex)

  def error[R <: Logger](msg: Json, ex: Throwable): RIO[R, Unit] =
    for {
      env <- ZIO.environment[R]
      result <- ZIO(env.error(msg.printWith(Printer.noSpaces), ex))
    } yield result

}
