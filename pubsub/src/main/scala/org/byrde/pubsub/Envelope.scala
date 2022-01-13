package org.byrde.pubsub

import java.util.UUID

case class Envelope[T](topic: String, msg: T, id: UUID = UUID.randomUUID)
