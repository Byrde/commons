package org.byrde.pubsub

import scala.util.Try

trait MutexSupport {
  @volatile protected var _locked = false

  protected def mutex[T](fn: =>T): Either[MutexState.Locked.type, Try[T]] =
    synchronized {
      if (!_locked) {
        _locked = true
        val result = Try(fn)
        _locked = false
        Right(result)
      } else Left(MutexState.Locked)
    }
}
