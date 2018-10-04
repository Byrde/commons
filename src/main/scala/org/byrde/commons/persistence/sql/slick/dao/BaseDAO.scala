package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.db.Db
import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.sqlbase.conf.Profile
import org.byrde.commons.persistence.sql.slick.sqlbase.table.Tables
import org.byrde.commons.persistence.sql.slick.{HasPrivilege, Role}

import slick.lifted.{CanBeQueryCondition, Tag}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

import scala.concurrent.Future
import scala.language.higherKinds

abstract class BaseDAO[R <: Role, Entity <: BaseEntity, TableType <: Tables#BaseTable[Entity]](val profile: Profile[R])(table: Tag => TableType) {
  protected val db =
    Db(profile)

  import profile.api._

  implicit class Query2Run[T, E <: Effect](query: DBIOAction[T, NoStream, E]) {
    def run(implicit ev: R HasPrivilege E): Future[T] =
      db.run(query)
  }

  val QueryBuilder =
    TableQuery(table)

  def findById(id: Long): SqlAction[Option[Entity], NoStream, Effect.Read] =
    QueryBuilder.withFilter(_.id === id).result.headOption

  def findByFilter[QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlStreamingAction[Seq[Entity], Entity, Effect.Read] =
    QueryBuilder.withFilter(f).result

  def inserts(rows: Entity*): FixedSqlAction[Seq[Long], NoStream, Effect.Write] =
    QueryBuilder returning QueryBuilder.map(_.id) ++= rows

  def deleteByIds(ids: Long*): FixedSqlAction[Int, NoStream, Effect.Write] =
    QueryBuilder.withFilter(_.id.inSet(ids)).delete

  def deleteByFilter[QueryCondition : CanBeQueryCondition](f: TableType => QueryCondition): FixedSqlAction[Int, NoStream, Effect.Write] =
    QueryBuilder.withFilter(f).delete

  def update[Value: ColumnType, QueryCondition: CanBeQueryCondition](f: TableType => QueryCondition)(value: Value, field: TableType => Rep[Value]): FixedSqlAction[Int, NoStream, Effect.Write] = {
    val query =
      for {
        c <- QueryBuilder.withFilter(f)
      } yield field(c)

    query.update(value)
  }
}