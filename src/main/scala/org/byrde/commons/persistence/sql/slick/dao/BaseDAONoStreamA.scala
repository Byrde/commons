package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.table.TablesA
import slick.jdbc.JdbcBackend
import slick.lifted.{CanBeQueryCondition, TableQuery}

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseDAONoStreamA[T <: TablesA#BaseTableA[A], A <: BaseEntity](tableQ: TableQuery[T])(implicit tables: TablesA, ec: ExecutionContext) {
  import tables.db
  import tables.profile.api._

  def run: Future[Seq[A]] =
    db.run(tableQ.result)

  def insert(row: A)(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[A] =
    inserts(Seq(row)).map(_.head)

  def inserts(rows: Seq[A])(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Seq[A]] = {
    val func = tableQ returning tableQ.map(_.id) ++= rows
    session.fold(db.run(func))(_.apply(db).database.run(func.withPinnedSession)).flatMap(rows => Future.sequence(rows.map(findById))).map(_.flatten)
  }

  def findById(id: Long)(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Option[A]] = {
    val func = tableQ.filter(_.id === id).result.headOption
    session.fold(db.run(func))(_.apply(db).database.run(func.withPinnedSession))
  }

  def findByFilter[C: CanBeQueryCondition](f: (T) => C)(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Seq[A]] = {
    val func = tableQ.withFilter(f).result
    session.fold(db.run(func))(_.apply(db).database.run(func.withPinnedSession))
  }

  def deleteById(id: Long)(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Int] =
    deleteByIds(Seq(id))

  def deleteByIds(ids: Seq[Long])(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Int] = {
    val func = tableQ.filter(_.id.inSet(ids)).delete
    session.fold(db.run(func))(_.apply(db).database.run(func.withPinnedSession))
  }

  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C)(implicit session: Option[JdbcBackend#Database => JdbcBackend#Session] = None): Future[Int] = {
    val func = tableQ.withFilter(f).delete
    session.fold(db.run(func))(_.apply(db).database.run(func.withPinnedSession))
  }

  def fields: Seq[String] =
    Seq.empty
}