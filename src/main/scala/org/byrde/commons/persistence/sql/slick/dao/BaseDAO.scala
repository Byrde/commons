package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.BaseEntity
import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.table.BaseTables

import slick.lifted.CanBeQueryCondition
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

abstract class BaseDAO[Entity <: BaseEntity, TableType <: BaseTables#BaseTable[Entity]](protected val profile: Profile[_]) {
  import profile.api._

  def QueryBuilder: slick.lifted.TableQuery[TableType]

  def findById(id: Long): SqlAction[Option[Entity], slick.dbio.NoStream, slick.dbio.Effect.Read] =
    QueryBuilder.withFilter(_.id === id).result.headOption

  def findByFilter[QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlStreamingAction[Seq[Entity], Entity, slick.dbio.Effect.Read] =
    QueryBuilder.withFilter(f).result

  def inserts(rows: Entity*): FixedSqlAction[Seq[Long], slick.dbio.NoStream, slick.dbio.Effect.Write] =
    QueryBuilder returning QueryBuilder.map(_.id) ++= rows

  def deleteByIds(ids: Long*): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] =
    QueryBuilder.withFilter(_.id.inSet(ids)).delete

  def deleteByFilter[QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] =
    QueryBuilder.withFilter(f).delete

  def update[Value: ColumnType, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(value: Value, field: TableType => slick.lifted.Rep[Value]): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] = {
    val query =
      for {
        c <- QueryBuilder.withFilter(f)
      } yield field(c)

    query.update(value)
  }
}