package org.byrde.commons.persistence.sql.slick

trait Role

object Role {
  trait Master extends Role
  trait Slave extends Role


}
