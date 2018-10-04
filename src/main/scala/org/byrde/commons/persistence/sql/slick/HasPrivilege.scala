package org.byrde.commons.persistence.sql.slick

import org.byrde.commons.persistence.sql.slick.Role.{Master, Slave}

import slick.dbio.Effect
import slick.dbio.Effect.{Read, Schema, Transactional, Write}

import scala.annotation.implicitNotFound

@implicitNotFound("'${R}' database is not privileged to to perform effect '${E}'.")
trait HasPrivilege[R <: Role, E <: Effect]

object HasPrivilege {
  type ReadWriteTransactionSchema = Read with Write with Transactional with Schema

  implicit val slaveCanRead: Slave HasPrivilege Read =
    null

  implicit val masterCanRead: Master HasPrivilege Read =
    null

  implicit val masterCanWrite: Master HasPrivilege Write =
    null

  implicit val masterCanPerformTransactions: Master HasPrivilege ReadWriteTransactionSchema =
    null
}