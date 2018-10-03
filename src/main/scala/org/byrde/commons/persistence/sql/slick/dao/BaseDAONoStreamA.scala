package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.table.TablesA

import slick.lifted.{CanBeQueryCondition, TableQuery}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

abstract class BaseDAONoStreamA[T <: TablesA#BaseTableA[TT], TT <: BaseEntity](tableQ: TableQuery[T])(implicit val tables: TablesA) {
  import tables.profile.api._

  def findById(id: Long): SqlAction[Option[TT], NoStream, Effect.Read] =
    tableQ
      .filter(_.id === id)
      .result
      .headOption

  def findByFilter[C: CanBeQueryCondition](f: T => C): FixedSqlStreamingAction[Seq[TT], TT, Effect.Read] =
    tableQ
      .withFilter(f)
      .result

  def inserts(rows: TT*): FixedSqlAction[Seq[Long], NoStream, Effect.Write] =
    tableQ returning tableQ.map(_.id) ++= rows

  def deleteByIds(ids: Long*): FixedSqlAction[Int, NoStream, Effect.Write] =
    tableQ
      .filter(_.id.inSet(ids))
      .delete

  def deleteByFilter[C : CanBeQueryCondition](f: T => C): FixedSqlAction[Int, NoStream, Effect.Write] =
    tableQ
      .withFilter(f)
      .delete
}