package org.byrde.slick

import org.byrde.slick.Role.{Master, Slave}
import org.byrde.slick.conf.Profile

import slick.dbio.Effect

import scala.annotation.implicitNotFound

@implicitNotFound("'${R}' database is not privileged to to perform effect '${E}'.")
class HasPrivilege[R <: Role, +E <: Effect](profile: Profile[R])

object HasPrivilege {
  implicit def SlaveCanRead(implicit profile: Profile[Slave]): Slave HasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanRead(implicit profile: Profile[Master]): Master HasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanWrite(implicit profile: Profile[Master]): Master HasPrivilege profile.api.Effect.Write =
    null

  implicit def MasterCanSchema(implicit profile: Profile[Master]): Master HasPrivilege profile.api.Effect.Schema =
    null

  implicit def MasterCanPerformTransactions(implicit profile: Profile[Master]): Master HasPrivilege profile.api.Effect.Write with profile.api.Effect.Transactional =
    null

  implicit def MasterCanAll(implicit profile: Profile[Master]): Master HasPrivilege profile.api.Effect.All =
    null
}
