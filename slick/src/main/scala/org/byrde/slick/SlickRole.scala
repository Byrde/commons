package org.byrde.slick

trait SlickRole

object SlickRole {
  trait Master extends SlickRole

  trait Slave extends SlickRole
}
