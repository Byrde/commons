package org.byrde.logging

import org.byrde.logging.Log.LogImpl

trait Log {
  def asMap: Map[String, String]

  def ++ (other: Log): Log = LogImpl(asMap ++ other.asMap)

  def ++ (other: Map[String, String]): Log = LogImpl(asMap ++ other)
}

object Log {
  case class LogImpl(asMap: Map[String, String]) extends Log

  lazy val empty: Log = LogImpl(Map.empty)

  def apply(asMap: Map[String, String]): Log = LogImpl(asMap)

  def apply(asMap: (String, String)*): Log = LogImpl(asMap.toMap)
}
