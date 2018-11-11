package org.byrde.slick.migrations.api

import java.util.UUID

import org.byrde.slick.conf.MigrationEngineConfig
import org.byrde.slick.migrations.api.ClaimStatus.{AlreadyCompleted, Claimed}

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class MigrationEngineAPI(database: Database, profile: JdbcProfile, migrations: NamedMigration*)
  (implicit executionContext: ExecutionContext, config: MigrationEngineConfig = MigrationEngineConfig(1.second, 1.second)) {
  import profile.api._

  private case class MigrationRow(migrationName: String, applicationId: UUID, status: String, dateApplied: Long)

  private class MigrationTable(tag: Tag) extends Table[MigrationRow](tag, "migrations") {
    def migrationName =
      column[String]("migration_name", O.PrimaryKey)

    def applicationId =
      column[UUID]("id", O.SqlType("UUID"))

    def status =
      column[String]("status")

    def dateApplied =
      column[Long]("date_applied")

    def * = (migrationName, applicationId, status, dateApplied) <> (MigrationRow.tupled, MigrationRow.unapply)
  }

  private val MigrationTableTQ =
    TableQuery[MigrationTable]

  def run(): Future[Unit] =
    migrations.foldLeft(createTable(0, 10)) {
      (prev, next) =>
        prev.flatMap(_ => runMigration(next))
    }

  private def createTable(retry: Int, limit: Int): Future[Unit] =
    database
      .run {
        MTable
          .getTables
          .flatMap { tables =>
            tables
              .find(_.name.name == MigrationTableTQ.baseTableRow.tableName)
              .map(_ => DBIO.successful(()))
              .getOrElse(MigrationTableTQ.schema.create)
          }
      }
      .recoverWith {
        case _ if retry < limit =>
          delay[Unit](config.createMigrationTableRetryDelay.toMillis, createTable(retry + 1, limit))

        case ex =>
          throw ex
      }

  private def delay[T](millis: Long, next: => Future[T]): Future[T] =
    Future(Thread.sleep(millis)).flatMap(_ => next)

  private def runMigration(migration: NamedMigration): Future[Unit] = {
    val id =
      UUID.randomUUID()

    for {
      _ <- {
        claimMigration(migration, id)
          .recover {
            case _ =>
          }
      }
      check <- checkClaimedOrCompleted(migration, id)
      _ <- {
        check match {
          case Claimed =>
            database.run(migration.migration())

          case AlreadyCompleted =>
            Future.successful({})
        }
      }
      _ <- {
        check match {
          case Claimed =>
            markAsCompleted(migration, id)

          case AlreadyCompleted =>
            Future.successful({})
        }
      }
    } yield {}
  }

  private def claimMigration(migration: NamedMigration, id: UUID): Future[Unit] =
    database
      .run(MigrationTableTQ += MigrationRow(migration.name, id, "Requested", System.currentTimeMillis()))
      .map(_ => ())

  private def checkClaimedOrCompleted(migration: NamedMigration, id: UUID): Future[ClaimStatus] = {
    val MigrationName =
      migration.name

    database
      .run(MigrationTableTQ.filter(_.migrationName === migration.name).result.headOption)
      .flatMap {
        case Some(MigrationRow(MigrationName, `id`, "Requested", _)) =>
          Future.successful(Claimed)

        case Some(MigrationRow(MigrationName, _, "Completed", _)) =>
          Future.successful(AlreadyCompleted)

        case Some(MigrationRow(MigrationName, _, "Requested", _)) =>
          delay(config.checkClusteredMigrationCompleteDelay.toMillis, checkClaimedOrCompleted(migration, id))

        case x =>
          Future.failed(new IllegalStateException(s"By now someone should have the migration claimed. $x"))
      }
  }

  private def markAsCompleted(migration: NamedMigration, id: UUID): Future[Unit] =
    database
      .run {
        val row =
          MigrationRow(
            migration.name,
            id,
            "Completed",
            System.currentTimeMillis()
          )

        MigrationTableTQ
          .filter(r => r.migrationName === migration.name && r.applicationId === id)
          .update(row)
      }
      .map(_ => ())
}
