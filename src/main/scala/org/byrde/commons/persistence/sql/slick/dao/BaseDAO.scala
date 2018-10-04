package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.sqlbase.conf.Profile
import org.byrde.commons.persistence.sql.slick.sqlbase.table.Tables

import slick.lifted.CanBeQueryCondition
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

abstract class BaseDAO[Entity <: BaseEntity, TableType <: Tables#BaseTable[Entity]](protected val profile: Profile) {
  import profile.api._

  def QueryBuilder: TableQuery[TableType]

  protected def findById(id: Long): SqlAction[Option[Entity], NoStream, Effect.Read] =
    QueryBuilder.withFilter(_.id === id).result.headOption

  protected def findByFilter[QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlStreamingAction[Seq[Entity], Entity, Effect.Read] =
    QueryBuilder.withFilter(f).result

  protected def inserts(rows: Entity*): FixedSqlAction[Seq[Long], NoStream, Effect.Write] =
    QueryBuilder returning QueryBuilder.map(_.id) ++= rows

  protected def deleteByIds(ids: Long*): FixedSqlAction[Int, NoStream, Effect.Write] =
    QueryBuilder.withFilter(_.id.inSet(ids)).delete

  protected def deleteByFilter[QueryCondition : CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlAction[Int, NoStream, Effect.Write] =
    QueryBuilder.withFilter(f).delete

  protected def update[Value: ColumnType, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(value: Value, field: TableType => Rep[Value]): FixedSqlAction[Int, NoStream, Effect.Write] = {
    val query =
      for {
        c <- QueryBuilder.withFilter(f)
      } yield field(c)

    query.update(value)
  }
}