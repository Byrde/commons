package org.byrde.pubsub

import java.util.concurrent.atomic.AtomicBoolean

import scala.util.Try

trait MutexSupport {
  protected val _locked: AtomicBoolean = new AtomicBoolean(false)

  protected def mutex[T](fn: =>T): Either[MutexState.Locked.type, Try[T]] =
    synchronized {
      if (!_locked.get()) {
        _locked.set(true)
        val result = Try(fn)
        _locked.set(false)
        Right(result)
      } else Left(MutexState.Locked)
    }
}
