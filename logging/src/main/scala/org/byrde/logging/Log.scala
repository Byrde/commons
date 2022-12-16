package org.byrde.logging

import org.byrde.logging.Log.LogImpl

trait Log {
  def logs: Seq[(String, String)]

  def sensitiveLogs: Seq[(String, String)] = Seq.empty

  def ++ (other: Log): Log = LogImpl(logs ++ other.logs, sensitiveLogs ++ other.sensitiveLogs)

  def ++ (other: (String, String)*): Log = LogImpl(logs ++ other, sensitiveLogs)

  def !++ (other: (String, String)*): Log = LogImpl(logs, sensitiveLogs ++ other)
}

object Log {
  case class LogImpl(logs: Seq[(String, String)], override val sensitiveLogs: Seq[(String, String)] = Seq.empty)
    extends Log

  lazy val empty: Log = LogImpl(Seq.empty, Seq.empty)

  def apply(asSeq: (String, String)*): Log = LogImpl(asSeq, Seq.empty)
}
