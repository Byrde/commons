package org.byrde.slick

import org.byrde.slick.SlickRole.{Master, Slave}

import slick.dbio.Effect

import scala.annotation.implicitNotFound

@implicitNotFound("'${R}' database is not privileged to perform effect '${E}'.")
class SlickHasPrivilege[R <: SlickRole, +E <: Effect](profile: SlickProfile[R])

object SlickHasPrivilege {
  implicit def SlaveCanRead(implicit profile: SlickProfile[Slave]): Slave SlickHasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanRead(implicit profile: SlickProfile[Master]): Master SlickHasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanWrite(implicit profile: SlickProfile[Master]): Master SlickHasPrivilege profile.api.Effect.Write =
    null

  implicit def MasterCanSchema(implicit profile: SlickProfile[Master]): Master SlickHasPrivilege profile.api.Effect.Schema =
    null

  implicit def MasterCanPerformTransactions(implicit profile: SlickProfile[Master]): Master SlickHasPrivilege profile.api.Effect.Write with profile.api.Effect.Transactional =
    null

  implicit def MasterCanAll(implicit profile: SlickProfile[Master]): Master SlickHasPrivilege profile.api.Effect.All =
    null
}
