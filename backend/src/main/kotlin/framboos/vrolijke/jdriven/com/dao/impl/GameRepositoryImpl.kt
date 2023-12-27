package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.GameRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class GameRepositoryImpl : ReadRepositoryImpl<Game>(Games), GameRepository {
    override fun rowToObject(row: ResultRow) = Game(
        id = row[Games.id].value,
        gridSize = row[Games.gridSize],
        wumpus = Wumpus(row[Wumpusses.id].value, listOf(row[Wumpusses.x], row[Wumpusses.y])),
        treasure = Treasure(row[Treasures.id].value, listOf(row[Treasures.x], row[Treasures.y])),
    )

    override suspend fun all() = dbQuery {
        Games
            .join(Wumpusses, INNER) { Games.id eq Wumpusses.gameId }
            .join(Treasures, INNER) { Games.id eq Treasures.gameId }
            .selectAll()
            .map(::rowToObject)
    }

    override suspend fun findById(id: Int) = dbQuery {
        Games
            .join(Wumpusses, INNER, onColumn = Games.id, otherColumn = Wumpusses.gameId)
            .join(Treasures, INNER, onColumn = Games.id, otherColumn = Treasures.gameId)
            .select { Games.id eq id }
            .singleOrNull()
            ?.let(::rowToObject)
            ?.copy(pits = pitRepo.findByGameId(id), players = listOf())
    }
}

val gameRepo: GameRepository = GameRepositoryImpl()
