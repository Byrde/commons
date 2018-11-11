package org.byrde.slick.migrations.engine

import org.byrde.slick.migrations.engine.AstHelpers.{ColumnInfo, IndexInfo, TableInfo}
import org.byrde.slick.migrations.engine.Migration.ReversibleMigration
import org.byrde.slick.migrations.engine.TableMigration.{IrreversibleTableMigration, TableMigrationData}

import slick.ast.FieldSymbol
import slick.driver.JdbcDriver
import slick.lifted._

/**
 * Factory for [[TableMigration]]s
 */
object TableMigration {
  /**
    * Internal data structure that stores schema manipulation operations to be performed on a table
    */
  protected [engine] case class TableMigrationData(
    // not reversible: insufficient info: don't know entire old table
    tableDrop: Boolean = false,
    // reverse: create=false, drop=true
    tableCreate: Boolean = false,
    // reverse: reverse rename
    tableRename: Option[String] = None,
    // reverse: drop instead of create
    columnsCreate: Seq[ColumnInfo] = Nil,
    // reverse: create instead of drop
    columnsDrop: Seq[ColumnInfo] = Nil,
    // reverse: reverse rename
    columnsRename: Map[ColumnInfo, String] = Map.empty,
    // not reversible: insufficient info: don't have old data
    columnsAlterType: Seq[ColumnInfo] = Nil,
    // not reversible: insufficient info: don't have old data
    columnsAlterDefault: Seq[ColumnInfo] = Nil,
    // not reversible: insufficient info: don't have old data
    columnsAlterNullability: Seq[ColumnInfo] = Nil,
    // reverse: drop instead of create
    foreignKeysCreate: Seq[ForeignKey] = Nil,
    // reverse: create instead of drop
    foreignKeysDrop: Seq[ForeignKey] = Nil,
    // reverse: drop instead of create
    primaryKeysCreate: Seq[(String, Seq[FieldSymbol])] = Nil,
    // reverse: create instead of drop
    primaryKeysDrop: Seq[(String, Seq[FieldSymbol])] = Nil,
    // reverse: drop instead of create
    indexesCreate: Seq[IndexInfo] = Nil,
    // reverse: create instead of drop
    indexesDrop: Seq[IndexInfo] = Nil,
    // reverse: reverse rename
    indexesRename: Map[IndexInfo, String] = Map.empty
  ) {
    override def productPrefix =
      "Table Migration of: "

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    private def ifStr(test: Boolean)(str: =>String) =
      if(test) str else ""

    private def wrap(data: Iterable[String]) =
      data.mkString("(",",",")")

    private def wrap(data: String) =
      "(" + data + ")"

    override def toString: String =
      "" +
        ifStr(tableDrop)                   ("dropTable") +
        ifStr(tableCreate)                 ("createTable") +
        ifStr(tableRename.isDefined)       ("renameTable to "+ wrap(tableRename.getOrElse("<error>"))) +
        ifStr(columnsCreate.nonEmpty)      ("addColumns" + wrap(columnsCreate.map(_.name))) +
        ifStr(columnsDrop.nonEmpty)        ("dropColumns" + wrap(columnsDrop.map(_.name))) +
        ifStr(columnsRename.nonEmpty)      ("renameColumns" + wrap(columnsRename.map{case (from, to) => from.name + " -> " + to})) +
        ifStr(columnsAlterType.nonEmpty)   ("alterColumnTypes" + wrap(columnsAlterType.map(c => c.name + "-" + c.sqlType))) +
        ifStr(columnsAlterDefault.nonEmpty)("alterColumnDefaults" + wrap(columnsAlterDefault.map(c => c.name + "-" + c.default.getOrElse("<null>")))) +
        ifStr(columnsAlterNullability.nonEmpty) ("alterColumnNullability" + wrap(columnsAlterNullability.map(c => c.name + "-" + !c.notNull))) +
        ifStr(foreignKeysCreate.nonEmpty)  ("addForeignKeys" + wrap(foreignKeysCreate.map{_.name})) +   //TODO the next few are messy, clean them up
        ifStr(foreignKeysDrop.nonEmpty)    ("dropForeignKeys" + wrap(foreignKeysDrop.map(_.name))) +
        ifStr(primaryKeysCreate.nonEmpty)  ("createPrimaryKeys" + wrap(primaryKeysCreate.map(_.toString))) +
        ifStr(primaryKeysDrop.nonEmpty)    ("dropPrimaryKeys" + wrap(primaryKeysDrop.map(_.toString))) +
        ifStr(indexesCreate.nonEmpty)      ("createIndexes" + wrap(indexesCreate.map(_.name))) +
        ifStr(indexesDrop.nonEmpty)        ("dropIndexes" + wrap(indexesCreate.map(_.name))) +
        ifStr(indexesRename.nonEmpty)      ("renameIndexes" + wrap(indexesRename.map{case (from, to) => from.name + " -> " + to}))
  }

  /**
    * The concrete [[TableMigration]] class used when irreversible operations are to be performed
    * (such as dropping a table)
    */
  final class IrreversibleTableMigration[T <: JdbcDriver#Table[_]] private [engine] (table: T, override val tableInfo: TableInfo, protected [engine] val data: TableMigrationData)(implicit dialect: Dialect[_]) extends TableMigration[T](table) {
    type Self =
      IrreversibleTableMigration[T]

    protected def withData(d: TableMigrationData) =
      new IrreversibleTableMigration(table, tableInfo, d)
  }

  /**
    * The concrete [[TableMigration]] class used when all operations are reversible.
    * This class extends [[org.byrde.slick.migrations.engine.Migration.ReversibleMigration]] and as such includes a [[reverse]] method
    * that returns a `TableMigration` that performs the inverse operations ("down migration").
    */
  final class ReversibleTableMigration[T <: JdbcDriver#Table[_]] private [engine] (table: T, protected[engine] val data: TableMigrationData)(implicit dialect: Dialect[_]) extends TableMigration[T](table) with ReversibleMigration {
    outer =>

    /*
     * This should be redundant since the constructor is private[api] and should only be called
     * when these requirements are known to hold.
     */
    require(!data.tableDrop)
    require(data.columnsAlterType.isEmpty)
    require(data.columnsAlterDefault.isEmpty)
    require(data.columnsAlterNullability.isEmpty)

    type Self = ReversibleTableMigration[T]
    protected def withData(d: TableMigrationData) = new ReversibleTableMigration(table, d)

    def reverse: IrreversibleTableMigration[T] = {
      new IrreversibleTableMigration(
        table,
        outer.data.tableRename match {
          case Some(name) =>
            outer.tableInfo.copy(tableName = name)

          case None       =>
            outer.tableInfo
        },
        data.copy(
          tableDrop               = data.tableCreate,
          tableCreate             = false,
          tableRename             = data.tableRename.map(_ => tableInfo.tableName),
          columnsCreate           = data.columnsDrop.reverse,
          columnsDrop             = data.columnsCreate.reverse,
          columnsRename           = data.columnsRename.map {
            case (ci, s) =>
              (ci.copy(name = s), ci.name)
          },
          columnsAlterType        = Nil,
          columnsAlterDefault     = Nil,
          columnsAlterNullability = Nil,
          foreignKeysCreate       = data.foreignKeysDrop.reverse,
          foreignKeysDrop         = data.foreignKeysCreate.reverse,
          primaryKeysCreate       = data.primaryKeysDrop.reverse,
          primaryKeysDrop         = data.primaryKeysCreate.reverse,
          indexesCreate           = data.indexesDrop.reverse,
          indexesDrop             = data.indexesCreate.reverse,
          indexesRename           = data.indexesRename.map {
            case (ii, s) =>
              (ii.copy(name = s), ii.name)
          }
        )
      )
    }
  }

  /**
   * Creates a [[TableMigration]] that will perform migrations on `table`
   */
  def apply[T <: JdbcDriver#Table[_]](table: T)(implicit dialect: Dialect[_]) =
    new ReversibleTableMigration(table, TableMigrationData())

  /**
   * Creates a [[TableMigration]] that will perform migrations on the table
   * referenced by `tableQuery`
   */
  def apply[T <: JdbcDriver#Table[_]](tableQuery: TableQuery[T])(implicit dialect: Dialect[_]) =
    new ReversibleTableMigration(tableQuery.baseTableRow, TableMigrationData())
}

/**
 * The base class for table migrations.
 * A table migration is a [[Migration]] that performs changes to a table.
 *
 * See this class's methods for a list of available operations. Calling an operation method
 * returns a new `TableMigration` that has that operation added, over operations
 * contained in the original `TableMigration`. This allows for a nice method chaining syntax.
 *
 * Like all [[Migration]]s, you can run the resulting [[TableMigration]] by calling its
 * `apply` method (it expects an implicit `Session`). Being an [[SqlMigration]] you
 * can also call the `sql` method to see the SQL statement(s) it uses.
 *
 * This class is abstract; use its companion object as a factory to get an instance.
 *
 * @example {{{
 *   object table1 extends Table[(Int, Int, Int)]("table1") {
 *     def col1 = column[Int]("col1")
 *     def col2 = column[Int]("col2")
 *     def col3 = column[Int]("col3")
 *     def * = col1 ~ col2 ~ col3
 *   }
 *   implicit val dialect = new H2Dialect
 *   val migration = TableMigration(table1)
 *                     .addColumns(_.col1, _.col2)
 *                     .addColumns(_.col3)
 * }}}
 * @groupname oper Schema Manipulation Operations
 */
sealed abstract class TableMigration[T <: JdbcDriver#Table[_]](table: T)(implicit dialect: Dialect[_])
  extends SqlMigration with AstHelpers with Equals { outer =>
  /**
   * The concrete type of this `TableMigration` ([[org.byrde.slick.migrations.engine.TableMigration.ReversibleTableMigration]] or [[org.byrde.slick.migrations.engine.TableMigration.IrreversibleTableMigration]]).* Operations that are in of themselves reversible will return an instance of this type.
   */
  type Self <: TableMigration[T]

  def tableInfo: TableInfo =
    TableInfo(table.schemaName, table.tableName)

  def sql: Seq[String] =
    dialect.migrateTable(tableInfo, data)

  protected [engine] def data: TableMigrationData

  protected def withData(data: TableMigrationData): Self

  private def colInfo(f: T => Rep[_]): ColumnInfo = {
    val col =
      f(table)

    fieldSym(col.toNode) match {
      case Some(c) =>
        table.tableProvider match {
          case driver: JdbcDriver =>
            columnInfo(driver, c)

          case _ =>
            sys.error("Invalid table: " + table)
        }

      case None =>
        sys.error("Invalid column: " + col)
    }
  }

  /**
   * Create the table.
   * Note: drop + create is allowed.
   * @group oper
   */
  def create: Self =
    withData(data.copy(tableCreate = true))

  /**
   * Drop the table.
   * Note: drop + create is allowed.
   * @group oper
   */
  def drop =
    new IrreversibleTableMigration(
      table,
      tableInfo,
      data.copy(
        tableCreate = false,
        tableDrop = true
      )
    )

  /**
   * Rename the table
   * @param to the new name for the table
   * @group oper
   */
  def rename(to: String): Self =
    withData(data.copy(tableRename = Some(to)))

  /**
   * Add columns to the table.
   * (If the table is being created, these may be incorporated into the `CREATE TABLE` statement.)
   * @param cols zero or more column-returning functions, which are passed the table object.
   * @example {{{ tblMig.addColumns(_.col1, _.col2, _.column[Int]("fieldNotYetInTableDef")) }}}
   * @group oper
   */
  def addColumns(cols: (T => Rep[_])*): Self =
    withData(data.copy(columnsCreate = data.columnsCreate ++ cols.map(colInfo)))

  /**
   * Drop columns.
   * @param cols zero or more column-returning functions, which are passed the table object.
   * @example {{{ tblMig.dropColumns(_.col1, _.col2, _.column[Int]("oldFieldNotInTableDef")) }}}
   * @group oper
   */
  def dropColumns(cols: (T => Rep[_])*): Self =
    withData(data.copy(columnsDrop = data.columnsDrop ++ cols.map(colInfo)))

  /**
   * Rename a column.
   * @param col a column-returning function, which is passed the table object.
   * @example {{{ tblMig.renameColumn(_.col1, "newName") }}}
   * @group oper
   */
  def renameColumn(col: T => Rep[_], to: String): Self =
    withData(data.copy(columnsRename = data.columnsRename + (colInfo(col) -> to)))

  /**
   * Changes the data type of columns based on the column definitions in `cols`
   * @param cols zero or more column-returning functions, which are passed the table object.
   * @example {{{ tblMig.alterColumnTypes(_.col1, _.column[NotTheTypeInTableDef]("col2")) }}}
   * @group oper
   */
  def alterColumnTypes(cols: (T => Rep[_])*) =
    new IrreversibleTableMigration(
      table,
      tableInfo,
      data.copy(columnsAlterType = data.columnsAlterType ++ cols.map(colInfo))
    )

  /**
   * Changes the default value of columns based on the column definitions in `cols`
   * @param cols zero or more column-returning functions, which are passed the table object.
   * @example {{{ tblMig.alterColumnDefaults(_.col1, _.column[Int]("col2", O.Default("notTheDefaultInTableDef"))) }}}
   * @group oper
   */
  def alterColumnDefaults(cols: (T => Rep[_])*) =
    new IrreversibleTableMigration(
      table,
      tableInfo,
      data.copy(columnsAlterDefault = data.columnsAlterDefault ++ cols.map(colInfo))
    )

  /**
   * Changes the nullability of columns based on the column definitions in `cols`
   * @param cols zero or more column-returning functions, which are passed the table object.
   * @example {{{ tblMig.alterColumnNulls(_.col1, _.column[Int]("col2", O.NotNull)) }}}
   * @group oper
   */
  def alterColumnNulls(cols: (T => Rep[_])*) =
    new IrreversibleTableMigration(
      table,
      tableInfo,
      data.copy(columnsAlterNullability = data.columnsAlterNullability ++ cols.map(colInfo))
    )

  /**
   * Adds primary key constraints.
   * @param pks zero or more `PrimaryKey`-returning functions, which are passed the table object.
   * @example {{{ tblMig.addPrimaryKeys(_.pkDef) }}}
   * @group oper
   */
  def addPrimaryKeys(pks: (T => PrimaryKey)*): Self =
    withData(
      data.copy(
        primaryKeysCreate =
          data.primaryKeysCreate ++
            pks.map { f =>
              val key =
                f(table)

              (key.name, key.columns.flatMap(node => fieldSym(node)))
            }
      )
    )

  /**
   * Drops primary key constraints.
   * @param pks zero or more `PrimaryKey`-returning functions, which are passed the table object.
   * @example {{{ tblMig.dropPrimaryKeys(_.pkDef) }}}
   * @group oper
   */
  def dropPrimaryKeys(pks: (T => PrimaryKey)*): Self =
    withData(
      data.copy(
        primaryKeysDrop =
          data.primaryKeysDrop ++
            pks.map { f =>
              val key =
                f(table)

              (key.name, key.columns.flatMap(node => fieldSym(node)))
            }
      )
    )

  /**
   * Adds foreign key constraints.
   * @param fkqs zero or more `ForeignKeyQuery`-returning functions, which are passed the table object.
   * @example {{{ tblMig.addForeignKeys(_.fkDef) }}}
   * @group oper
   */
  def addForeignKeys(fkqs: (T => ForeignKeyQuery[_ <: AbstractTable[_], _])*): Self =
    withData(
      data.copy(
        foreignKeysCreate =
          data.foreignKeysCreate ++
            fkqs.flatMap { f =>
              val fkq =
                f(table)

              fkq.fks: Seq[ForeignKey]
            }
      )
    )

  /**
   * Drops foreign key constraints.
   * @param fkqs zero or more `ForeignKeyQuery`-returning functions, which are passed the table object.
   * @example {{{ tblMig.dropForeignKeys(_.fkDef) }}}
   * @group oper
   */
  def dropForeignKeys(fkqs: (T => ForeignKeyQuery[_ <: AbstractTable[_], _])*): Self =
    withData(
      data.copy(
        foreignKeysDrop =
          data.foreignKeysDrop ++
            fkqs.flatMap { f =>
              val fkq =
                f(table)

              fkq.fks: Seq[ForeignKey]
            }
        )
    )

  /**
   * Adds indexes
   * @param indexes zero or more `Index`-returning functions, which are passed the table object.
   * @example {{{ tblMig.addIndexes(_.idxDef) }}}
   * @group oper
   */
  def addIndexes(indexes: (T => Index)*): Self =
    withData(
      data.copy(
        indexesCreate =
          data.indexesCreate ++
            indexes.map { f =>
              val i =
                f(table)

              indexInfo(i)
            }
      )
    )

  /**
   * Drops indexes
   * @param indexes zero or more `Index`-returning functions, which are passed the table object.
   * @example {{{ tblMig.dropIndexes(_.idxDef) }}}
   * @group oper
   */
  def dropIndexes(indexes: (T => Index)*): Self =
    withData(
      data.copy(
        indexesDrop =
          data.indexesDrop ++
            indexes.map { f =>
              val i =
                f(table)

              indexInfo(i)
            }
      )
    )

  /**
   * Renames an index
   * @param index an `Index`-returning function, which is passed the table object.
   * @example {{{ tblMig.renameIndex(_.idxDef, "newName") }}}
   * @group oper
   */
  def renameIndex(index: (T => Index), to: String): Self =
    withData(data.copy(indexesRename = data.indexesRename + (indexInfo(index(table)) -> to)))

  def canEqual(that: Any): Boolean =
    that.isInstanceOf[TableMigration[_]]

  override def equals(a: Any): Boolean =
    a match {
      case that: TableMigration[_] if that canEqual this =>
        that.tableInfo == this.tableInfo && that.data == this.data

      case _ =>
        false
    }

  override def toString =
    s"TableMigration{${data.toString}}"
}
