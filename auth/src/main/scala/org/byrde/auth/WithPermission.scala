package org.byrde.auth

trait WithPermission[T <: Permission] {
  def permission: T
}
