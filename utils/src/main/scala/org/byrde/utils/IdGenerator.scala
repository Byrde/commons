package org.byrde.utils

import java.util.{Random, UUID}

object IdGenerator {
  private type Id = String

  private val Chars = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890!@#$"

  def generateId: UUID =
    UUID.randomUUID

  def simplify(id: UUID): Id =
    id.toString

  def generateSimpleId: Id =
    simplify(generateId)

  def generateShortId(idLength: Int): Id = {
    val random = new Random
    val token = new StringBuilder(idLength)
    var i = 0
    while (i < idLength) {
      token.append(Chars.charAt(random.nextInt(Chars.length)))
      i = i + 1
    }
    token.toString
  }
}
