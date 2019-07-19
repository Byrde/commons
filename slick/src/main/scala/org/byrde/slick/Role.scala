package org.byrde.slick

trait Role

object Role {
  trait Master extends Role

  trait Slave extends Role
}
