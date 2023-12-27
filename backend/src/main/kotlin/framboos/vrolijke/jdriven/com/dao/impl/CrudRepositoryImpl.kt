package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CrudRepository
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.Dto
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

abstract class CrudRepositoryImpl<Creator, D : Dto> (
    private val table: IntIdTable
) : CrudRepository<Creator, D> {

    abstract fun rowToObject(row: ResultRow) : D
    abstract fun insert(it: InsertStatement<Number>, creator: Creator)
    abstract fun update(it: UpdateStatement, dto: D)

    override suspend fun all() = dbQuery {
        table.selectAll().map(::rowToObject)
    }

    override suspend fun findById(id: Int) = dbQuery {
        table
            .select { table.id eq id }
            .map(::rowToObject)
            .singleOrNull()
    }

    override suspend fun add(creator: Creator) = dbQuery {
        val insertStatement = table.insert { insert(it, creator) }
        insertStatement.resultedValues?.singleOrNull()?.let(::rowToObject)
    }

    override suspend fun edit(dto: D) = dbQuery {
        table.update({ table.id eq dto.id }) { update(it, dto) } > 0
    }

    override suspend fun delete(id: Int) = dbQuery {
        table.deleteWhere { table.id eq id } > 0
    }
}
