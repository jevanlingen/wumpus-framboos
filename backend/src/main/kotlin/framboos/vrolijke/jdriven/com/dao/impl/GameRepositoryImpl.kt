package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.GameRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class GameRepositoryImpl : ReadRepositoryImpl<Game>(Games), GameRepository {
    override fun table() = Games
        .join(Wumpusses, INNER, onColumn = Games.id, otherColumn = Wumpusses.gameId)
        .join(Treasures, INNER, onColumn = Games.id, otherColumn = Treasures.gameId)

    override fun toDto(row: ResultRow) = Game(
        id = row[Games.id].value,
        gridSize = row[Games.gridSize],
        wumpus = Wumpus(row[Wumpusses.id].value, listOf(row[Wumpusses.x], row[Wumpusses.y])),
        treasure = Treasure(row[Treasures.id].value, listOf(row[Treasures.x], row[Treasures.y])),
    )

    override suspend fun allIds(): List<Int> = dbQuery {
        Games
            .slice(Games.id)
            .selectAll()
            .map { it[Games.id].value }
    }

        override suspend fun findById(id: Int) =
            super.findById(id)
                ?.copy(pits = pitRepo.findByGameId(id), players = playerRepo.findByGameId(id))
    }

val gameRepo: GameRepository = GameRepositoryImpl()
