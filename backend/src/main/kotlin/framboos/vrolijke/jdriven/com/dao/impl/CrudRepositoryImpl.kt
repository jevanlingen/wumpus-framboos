package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CrudRepository
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.Dto
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

abstract class CrudRepositoryImpl<Creator, D : Dto> (
    private val table: IntIdTable
) : ReadRepositoryImpl<D>(table), CrudRepository<Creator, D> {

    abstract fun insert(it: InsertStatement<Number>, creator: Creator)
    abstract fun update(it: UpdateStatement, dto: D)

    override suspend fun add(creator: Creator) = dbQuery {
        val insertStatement = table.insert { insert(it, creator) }
        insertStatement.resultedValues?.singleOrNull()?.let(::toDto)
    }

    override suspend fun edit(dto: D) = dbQuery {
        table.update({ table.id eq dto.id }) { update(it, dto) } > 0
    }

    override suspend fun deleteById(id: Int) = dbQuery {
        table.deleteWhere { table.id eq id } > 0
    }
}
