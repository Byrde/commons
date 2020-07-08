package org.byrde.pubsub

import java.util.UUID

case class Envelope[T](topic: Topic, msg: T, id: Id = UUID.randomUUID)
