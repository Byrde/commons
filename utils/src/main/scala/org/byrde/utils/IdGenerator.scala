package org.byrde.utils

import java.util.UUID

object IdGenerator {
  def generateId: UUID =
    UUID.randomUUID
}
