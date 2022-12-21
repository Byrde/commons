package org.byrde.pubsub

sealed trait MutexState

object MutexState {
  case object Locked extends MutexState
}
