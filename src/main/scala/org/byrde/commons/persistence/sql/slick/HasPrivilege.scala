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

  implicit val slaveCanRead: Slave HasPrivilege Read =
    null

  implicit val masterCanRead: Master HasPrivilege Read =
    null

  implicit val masterCanWrite: Master HasPrivilege Write =
    null

  implicit val masterCanSchema: Master HasPrivilege Schema =
    null

  implicit val masterCanPerformTransactions: Master HasPrivilege WriteTransaction =
    null

  implicit val masterCanPerformReadTransactions: Master HasPrivilege ReadWriteTransaction =
    null
}