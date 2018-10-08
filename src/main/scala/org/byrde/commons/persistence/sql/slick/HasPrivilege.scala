package org.byrde.commons.persistence.sql.slick

import org.byrde.commons.persistence.sql.slick.Role.{Master, Slave}

import slick.dbio.Effect
import slick.dbio.Effect.{Read, Schema, Transactional, Write}

import scala.annotation.implicitNotFound

@implicitNotFound("'${R}' database is not privileged to to perform effect '${E}'.")
trait HasPrivilege[R <: Role, E <: Effect]

object HasPrivilege {
  type WriteTransaction = Write with Transactional

  type ReadWriteTransaction = Read with Write with Transactional

  implicit val SlaveCanRead: Slave HasPrivilege Read =
    null

  implicit val MasterCanRead: Master HasPrivilege Read =
    null

  implicit val MasterCanWrite: Master HasPrivilege Write =
    null

  implicit val MasterCanSchema: Master HasPrivilege Schema =
    null

  implicit val MasterCanPerformTransactions: Master HasPrivilege WriteTransaction =
    null

  implicit val MasterCanPerformReadTransactions: Master HasPrivilege ReadWriteTransaction =
    null
}