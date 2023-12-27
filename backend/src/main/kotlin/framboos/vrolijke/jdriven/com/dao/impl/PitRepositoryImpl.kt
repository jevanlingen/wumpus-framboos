package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.PitRepository
import framboos.vrolijke.jdriven.com.dao.model.Pit
import framboos.vrolijke.jdriven.com.dao.model.Pits
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

class PitRepositoryImpl : ReadRepositoryImpl<Pit>(Pits), PitRepository {
    override fun rowToObject(row: ResultRow) = Pit(
        id = row[Pits.id].value,
        coordinate = listOf(row[Pits.x], row[Pits.y])
    )

    override suspend fun findByGameId(gameId: Int) = dbQuery {
        Pits
            .select { Pits.gameId eq gameId }
            .map(::rowToObject)
    }
}

val pitRepo: PitRepository = PitRepositoryImpl()