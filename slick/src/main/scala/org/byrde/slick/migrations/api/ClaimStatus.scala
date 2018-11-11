package org.byrde.slick.migrations.api

sealed trait ClaimStatus

object ClaimStatus {
  case object Claimed extends ClaimStatus
  case object AlreadyCompleted extends ClaimStatus
}