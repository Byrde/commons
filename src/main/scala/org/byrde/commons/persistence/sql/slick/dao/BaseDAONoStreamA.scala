package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.table.TablesA

import slick.lifted.{CanBeQueryCondition, TableQuery}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

abstract class BaseDAONoStreamA[TableType <: TablesA#BaseTableA[Entity], Entity <: BaseEntity](protected val tableQ: TableQuery[TableType])(implicit val tables: TablesA) {
  import tables.profile.api._

  def findById(id: Long): SqlAction[Option[Entity], NoStream, Effect.Read] =
    tableQ
      .filter(_.id === id)
      .result
      .headOption

  def findByFilter[QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlStreamingAction[Seq[Entity], Entity, Effect.Read] =
    tableQ
      .withFilter(f)
      .result

  def inserts(rows: Entity*): FixedSqlAction[Seq[Long], NoStream, Effect.Write] =
    tableQ returning tableQ.map(_.id) ++= rows

  def deleteByIds(ids: Long*): FixedSqlAction[Int, NoStream, Effect.Write] =
    tableQ
      .filter(_.id.inSet(ids))
      .delete

  def deleteByFilter[QueryCondition : CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlAction[Int, NoStream, Effect.Write] =
    tableQ
      .withFilter(f)
      .delete

  def update[Value: ColumnType, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(value: Value, field: TableType => Rep[Value]): FixedSqlAction[Int, NoStream, Effect.Write] = {
    val query =
      for {
        c <- tableQ.withFilter(f)
      } yield field(c)

    query.update(value)
  }
}