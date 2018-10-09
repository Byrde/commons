package org.byrde.commons.persistence.sql.slick

import org.byrde.commons.persistence.sql.slick.Role.{Master, Slave}
import org.byrde.commons.persistence.sql.slick.conf.Profile

import slick.dbio.Effect

import scala.annotation.implicitNotFound

@implicitNotFound("'${R}' database is not privileged to to perform effect '${E}'.")
class HasPrivilege[R <: Role, +E <: Effect](profile: Profile[R])

object HasPrivilege {
  implicit def SlaveCanRead[R <: Role](implicit profile: Profile[R]): Slave HasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanRead[R <: Role](implicit profile: Profile[R]): Master HasPrivilege profile.api.Effect.Read =
    null

  implicit def MasterCanWrite[R <: Role](implicit profile: Profile[R]): Master HasPrivilege profile.api.Effect.Write =
    null

  implicit def MasterCanSchema[R <: Role](implicit profile: Profile[R]): Master HasPrivilege profile.api.Effect.Schema =
    null

  implicit def MasterCanPerformTransactions[R <: Role](implicit profile: Profile[R]): Master HasPrivilege profile.api.Effect.Write with profile.api.Effect.Transactional =
    null
}