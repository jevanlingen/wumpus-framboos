package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CrudRepository
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.Dto
import framboos.vrolijke.jdriven.com.dao.model.Entity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

abstract class CrudRepositoryImpl<Creator, D : Dto, in T : Entity> (
    private val table: T
) : CrudRepository<Creator, D> {

    abstract fun rowToObject(row: ResultRow) : D
    abstract fun insert(it: InsertStatement<Number>, entity: Creator)
    abstract fun update(it: UpdateStatement, entity: D)

    override suspend fun all() = dbQuery {
        table.selectAll().map(::rowToObject)
    }

    override suspend fun getById(id: Int) = dbQuery {
        table
            .select { table.id eq id }
            .map(::rowToObject)
            .singleOrNull()
    }

    override suspend fun add(entity: Creator) = dbQuery {
        val insertStatement = table.insert { insert(it, entity) }
        insertStatement.resultedValues?.singleOrNull()?.let(::rowToObject)
    }

    override suspend fun edit(entity: D) = dbQuery {
        table.update({ table.id eq entity.id }) { update(it, entity) } > 0
    }

    override suspend fun delete(id: Int) = dbQuery {
        table.deleteWhere { table.id eq id } > 0
    }
}
