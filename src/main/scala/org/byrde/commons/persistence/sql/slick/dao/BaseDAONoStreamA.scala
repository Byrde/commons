package org.byrde.commons.persistence.sql.slick.dao

import org.byrde.commons.persistence.sql.slick.sqlbase.BaseEntity
import org.byrde.commons.persistence.sql.slick.table.TablesA
import slick.lifted.{CanBeQueryCondition, TableQuery}

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseDAONoStreamA[T <: TablesA#BaseTableA[TT], TT <: BaseEntity](tableQ: TableQuery[T])(implicit tables: TablesA, ec: ExecutionContext) {
  import tables.db
  import tables.profile.api._

  def run: Future[Seq[TT]] =
    db.run(tableQ.result)

  def inserts(rows: TT*): Future[Seq[TT]] = {
    val func =
      tableQ returning tableQ.map(_.id) ++= rows

    db.run(func)
      .flatMap { rows =>
        Future.sequence(rows.map(findById))
      }
      .map(_.flatten)
  }

  def findById(id: Long): Future[Option[TT]] = {
    val func =
      tableQ
        .filter(_.id === id)
        .result
        .headOption

    db.run(func)
  }

  def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[TT]] = {
    val func =
      tableQ
        .withFilter(f)
        .result

    db.run(func)
  }

  def deleteByIds(ids: Long*): Future[Int] = {
    val func =
      tableQ
        .filter(_.id.inSet(ids))
        .delete

    db.run(func)
  }

  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int] = {
    val func =
      tableQ
        .withFilter(f)
        .delete

    db.run(func)
  }

  def fields: Seq[String] =
    Seq.empty
}