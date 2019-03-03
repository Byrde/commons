package org.byrde.auth

trait HasPrivilege[T, TT <: Permission] {
  def entity: T

  def permission: TT
}

object HasPrivilege {
  def apply[T, TT <: Permission](_entity: T, _type: TT): HasPrivilege[T, TT] =
    new HasPrivilege[T, TT] {
      override def entity: T =
        _entity

      override def permission: TT =
        _type
    }
}