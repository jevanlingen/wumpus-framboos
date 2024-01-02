package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.ReadRepository
import framboos.vrolijke.jdriven.com.dao.model.Dto
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

abstract class ReadRepositoryImpl<D : Dto> (
    private val table: IntIdTable
) : ReadRepository<D> {

    abstract fun toDto(row: ResultRow) : D

    open fun table() : FieldSet = table

    override suspend fun all() = dbQuery {
        table().selectAll().map(::toDto)
    }

    override suspend fun findById(id: Int) = dbQuery {
        table()
            .select { table.id eq id }
            .map(::toDto)
            .singleOrNull()
    }
}
