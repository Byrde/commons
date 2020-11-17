package org.byrde.tapir

trait TapirResponseAdaptor[A, T <: TapirResponse] {
  def apply(value: A): T
}