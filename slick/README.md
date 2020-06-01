# Slick [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/slick_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/slick_2.13/latest/)

Library to get setup with basic project structuring and utilities.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "slick" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
Follow this guide to get setup with roles, migrations, structure, and convenience when working with Slick. 

#### Implement Tables
```scala
trait Tables[R <: Role] {
  self: Profile[R] =>

  case class Row(id: Long)

  case class MyTable(_tableTag: Tag) extends Table[Row](_tableTag, "my_table") {
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def * = (id) <> ((Row.apply _).tupled, Row.unapply)
  }

  lazy val MyTableTQ: TableQuery[MyTable] =
    new TableQuery(MyTable)

}
```

#### Implement `Master` & `Slave` DAO
```scala
class MasterDAO() extends DAO[Master] {
  self: Profile[Master] with Db[Master] with Tables[Master] =>

  def insert(row: Row): Future[Id] =
    (MyTableTQ returning MyTableTQ.map(_.id) += row).run

}

class SlaveDAO() extends DAO[Slave] {
  self: Profile[Slave] with Db[Slave] with Tables[Slave] =>

  def findById(id: Long): Future[Option[Row]] =
    MyTableTQ.filter(_.id === id).result.headOption.run

}
```

#### Implement `Master` & `Slave` DAOs
```scala
class MasterDAOS(val config: DatabaseConfig[Master]) 
  extends Profile[Master]
  with Db[Master]
  with Tables[Master] {
  self =>

  trait Mixin extends Profile[Master] with Db[Master] with Tables[Master] {
    override def config: DatabaseConfig[Master] =
      self.config
  }

  val dao = new MasterDAO() with Mixin

}

class SlaveDAOS(val config: DatabaseConfig[Master]) 
  extends Profile[Slave]
  with Db[Slave]
  with Tables[Slave] {
  self =>

  trait Mixin extends Profile[Slave] with Db[Slave] with Tables[Slave] {
    override def config: DatabaseConfig[Slave] =
      self.config
  }

  val dao = new SlaveDAO() with Mixin

}
```

Note: By using the `Master` and `Slave` roles, we can ensure that read, write, and transaction operations can only happen on the appropriate database. For example, when creating scalable relational database infrastructure where there is one master database and several slave databases where only read operations are allowed.

#### Write on Master Database
```scala
object MasterConfig extends DatabaseConfig[Master] {
  override lazy val jdbc: slick.basic.DatabaseConfig[JdbcProfile] =
    slick.basic.DatabaseConfig.forConfig("slick.master")
}

val daos = new MasterDAOS(MasterConfig)

daos.insert(Row(1L))
```

#### Write on Slave Database (Error)
```scala
class WriteSlaveDAO() extends SlaveDAO[Slave] {
  self: Profile[Slave] with Db[Slave] with Tables[Slave] =>

  def insert(id: Long): Future[Id] =
    // 'org.byrde.slick.Role.Slave' database is not privileged to perform effect 'slick.dbio.Effect.Write'.
    (MyTableTQ returning MyTableTQ.map(_.id) += row).run 

}
```

Note: See the `Master` / `Slave` privileges here `SlickHasPrivilege`

## Migrations
Included is an engine for evolving SQL schemas
```scala
class v000__Baseline extends NamedMigration {
  self: Tables[Master] with Profile[Master] =>
  import profile.api._

  override lazy val name: String =
    "v000__Baseline"

  implicit def actionToMigration(action: DBIO[Unit]): Migration =
    () => action

  lazy val migration: Migration =
    MyTableTQ.schema.create
}

lazy val mig0 =
  new v000__Baseline with Profile[Master] with Tables[Master] {
    override def config: DatabaseConfig[Master] = self.config.MasterConfig
  }

lazy val migrations: Seq[NamedMigration] =
  Seq(mig0)

lazy val engine =
  new MigrationEngine(migrations) with Db[Master] with Profile[Master] {
    override def config: DatabaseConfig[Master] = MasterConfig
  }

implicit def AsEvidence: Profile[Master] =
  engine

Await.result(engine.migrate, 5.minute)
```