package org.byrde.play.support

trait PlaySupport
  extends BodyWritableSupport
    with CirceWritableSupport
    with WSResponseSupport

object PlaySupport extends PlaySupport