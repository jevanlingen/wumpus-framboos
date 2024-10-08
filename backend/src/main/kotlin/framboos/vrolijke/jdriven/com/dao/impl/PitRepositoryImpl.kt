package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.PitRepository
import framboos.vrolijke.jdriven.com.dao.model.Coordinate
import framboos.vrolijke.jdriven.com.dao.model.Pit
import framboos.vrolijke.jdriven.com.dao.model.Pits
import org.jetbrains.exposed.sql.*

class PitRepositoryImpl : ReadRepositoryImpl<Pit>(Pits), PitRepository {
    override fun toDto(row: ResultRow) = Pit(
        id = row[Pits.id].value,
        coordinate = Coordinate(row[Pits.x], row[Pits.y])
    )

    override suspend fun findByGameId(gameId: Int) = dbQuery {
        Pits.selectAll().where { Pits.gameId eq gameId }.map(::toDto)
    }
}

val pitRepo: PitRepository = PitRepositoryImpl()
