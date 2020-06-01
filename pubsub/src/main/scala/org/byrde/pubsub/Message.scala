package org.byrde.pubsub

case class Message[T](id: Id, message: T, topic: Topic)
