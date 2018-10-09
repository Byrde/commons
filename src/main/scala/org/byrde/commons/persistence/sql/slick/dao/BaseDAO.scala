package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.{BaseEntity, Role}
import org.byrde.commons.persistence.sql.slick.conf.Profile
import org.byrde.commons.persistence.sql.slick.table.BaseTables

import slick.lifted.{CanBeQueryCondition, TableQuery}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

abstract class BaseDAO[TableType <: BaseTables#BaseTable[Entity], Entity <: BaseEntity](val QueryBuilder: TableQuery[TableType]) {
  def findById[R <: Role](id: Long)(profile: Profile[R]): SqlAction[Option[Entity], slick.dbio.NoStream, slick.dbio.Effect.Read] = {
    import profile.api._

    QueryBuilder.withFilter(_.id === id).result.headOption
  }

  def findByFilter[R <: Role, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(profile: Profile[R]): FixedSqlStreamingAction[Seq[Entity], Entity, slick.dbio.Effect.Read] = {
    import profile.api._

    QueryBuilder.withFilter(f).result
  }

  def inserts[R <: Role](rows: Entity*)(profile: Profile[R]): FixedSqlAction[Seq[Long], slick.dbio.NoStream, slick.dbio.Effect.Write] = {
    import profile.api._

    QueryBuilder returning QueryBuilder.map(_.id) ++= rows
  }

  def deleteByIds[R <: Role](ids: Long*)(profile: Profile[R]): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] = {
    import profile.api._

    QueryBuilder.withFilter(_.id.inSet(ids)).delete
  }

  def deleteByFilter[R <: Role, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(profile: Profile[R]): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] = {
    import profile.api._

    QueryBuilder.withFilter(f).delete
  }

  def update[R <: Role, QueryCondition: CanBeQueryCondition, Value: profile.api.ColumnType](f: TableType => QueryCondition)(value: Value, field: TableType => slick.lifted.Rep[Value])(profile: Profile[R]): FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write] = {
    import profile.api._

    val query =
      for {
        c <- QueryBuilder.withFilter(f)
      } yield field(c)

    query.update(value)
  }
}